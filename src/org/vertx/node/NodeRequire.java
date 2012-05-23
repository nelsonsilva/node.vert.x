package org.vertx.node;


import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.ModuleScope;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;

public class NodeRequire extends org.mozilla.javascript.commonjs.module.Require
{
  static final ThreadLocal<ModuleScope> moduleScope = new ThreadLocal<>();

  private static boolean sandboxed = false;

  public NodeRequire(final ClassLoader cl, Context cx, Scriptable nativeScope) {
    super(cx, nativeScope, getModuleScriptProvider(cl), getPreExec(), getPostExec(), sandboxed);
  }

  static ModuleScriptProvider getModuleScriptProvider(final ClassLoader cl){
     return  new SoftCachingModuleScriptProvider(new NodeModuleSourceProvider(cl));
  }

  static Script getPreExec(){
    return new Script() {
      @Override
      public Object exec(Context context, Scriptable scope) {
        String js = "__dirname = module.uri.substring(\"file:\".length, module.uri.lastIndexOf('/'))";
        return context.evaluateString(scope, js,"preExec",1,null);
      }
    };
  }

  static Script getPostExec(){
    return new Script() {
      @Override
      public Object exec(Context context, Scriptable scope) {
        String js = "if(typeof vertxStop == 'function'){ " +
                "module.exports.vertxStop = vertxStop;" +
                "}";
        return context.evaluateString(scope, js, "postExec", 1, null);
      }
    };
  }

  // Applied pull request ......
  // Store current moduleScope
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                     Object[] args)
  {

    ModuleScope mScope = (ModuleScope) thisObj;
    moduleScope.set(mScope);

    return super.call(cx, scope, thisObj, args);
  }
}