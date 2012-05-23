/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.node;

import org.mozilla.javascript.*;
import org.mozilla.javascript.commonjs.module.Require;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;

import java.io.*;

/**
 * @author <a href="http://about.me/nelson.silva">Nelson Silva</a>
 */
public class NodeVerticle extends Verticle {

  private Function stopFunction;
  private ScriptableObject scope;
  private JsonObject config;
  private Logger log;

  private static ThreadLocal<ScriptableObject> scopeThreadLocal = new ThreadLocal<>();
  private static ThreadLocal<ClassLoader> clThreadLocal = new ThreadLocal<>();

  public NodeVerticle() {}

  protected String getMandatoryStringConfig(String fieldName) {
    String s = config.getString(fieldName);
    if (s == null) {
      throw new IllegalArgumentException(fieldName + " must be specified in config for busmod");
    }
    return s;
  }

  public static void load(String moduleName) throws Exception {
    ScriptableObject scope = scopeThreadLocal.get();
    ClassLoader cl = clThreadLocal.get();
    Context cx = Context.getCurrentContext();
    cx.setOptimizationLevel(0);
    loadScript(cl, cx, scope, moduleName);
  }

  private static void loadScript(ClassLoader cl, Context cx, ScriptableObject scope, String scriptName) throws Exception {
    InputStream is = cl.getResourceAsStream(scriptName);
    if (is == null) {
      throw new FileNotFoundException("Cannot find script: " + scriptName);
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    cx.evaluateReader(scope, reader, scriptName, 1, null);
    try {
      is.close();
    } catch (IOException ignore) {
    }
  }

  private static void addStandardObjectsToScope(ScriptableObject scope) {
    Object jsStdout = Context.javaToJS(System.out, scope);
    ScriptableObject.putProperty(scope, "stdout", jsStdout);
    Object jsStderr = Context.javaToJS(System.err, scope);
    ScriptableObject.putProperty(scope, "stderr", jsStderr);
  }

  private void addNodeObjectsToScope(ClassLoader cl, Context cx, ScriptableObject scope) throws Exception {
    loadScript(cl, cx, scope, "./lib/process.js");
    loadScript(cl, cx, scope, "./lib/timers.js");
  }

  // Support for loading from CommonJS modules
  protected Require installRequire(final ClassLoader cl, Context cx, ScriptableObject scope) {
    return new NodeRequire(cl, cx, scope);
  }


  public void start() throws Exception {
    config = container.getConfig();
    log = container.getLogger();

    ClassLoader cl = this.getClass().getClassLoader();

    String scriptName = getMandatoryStringConfig("main");

    if(! (scriptName.startsWith("./") || scriptName.startsWith("/") || scriptName.startsWith("../")) ){
      scriptName = "./" + scriptName;
    }

    Context cx = Context.enter();
    try {
      scope = cx.initStandardObjects();

      addStandardObjectsToScope(scope);
      addNodeObjectsToScope(cl, cx, scope);
      scope.defineFunctionProperties(new String[] { "load" }, NodeVerticle.class, ScriptableObject.DONTENUM);

      // This is pretty ugly - we have to set some thread locals so we can get a reference to the scope and
      // classloader in the load() method - this is because Rhino insists load() must be static
      scopeThreadLocal.set(scope);
      clThreadLocal.set(cl);

      Require require = installRequire(cl, cx, scope);

      Scriptable script = require.requireMain(cx, scriptName);
      try {
        stopFunction = (Function) script.get("vertxStop", scope);
      } catch (ClassCastException e) {
        // Get CCE if no such function
        stopFunction = null;
      }

    } finally {
      Context.exit();
    }
  }

  public void stop() throws Exception {
    if (stopFunction != null) {
      Context cx = Context.enter();
      try {
        stopFunction.call(cx, scope, scope, null);
      } finally {
        Context.exit();
      }
    }
  }
}
