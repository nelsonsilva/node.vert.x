package org.vertx.node;

import org.mozilla.javascript.commonjs.module.ModuleScope;
import org.mozilla.javascript.commonjs.module.provider.ModuleSource;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class NodeModuleSourceProvider extends UrlModuleSourceProvider {
  private ClassLoader cl;

  public NodeModuleSourceProvider(ClassLoader cl) {
    super(getPrivilegedURIs(cl), null);
    this.cl = cl;
  }

  private static List<URI> getPrivilegedURIs(ClassLoader cl){
    URL modulesURI = cl.getResource("./modules");
    List<URI> uris = new ArrayList<>();
    try {
      uris.add(modulesURI.toURI());
    } catch (URISyntaxException e) {

    }
    return uris;
  }

  @Override
  protected ModuleSource loadFromPrivilegedLocations(String moduleId, Object validator) throws IOException, URISyntaxException {

    // This is a core module
    if(!isRelativeModule(moduleId)){

      // If loading from classpath get a proper URI
      // Must check for each possible file to avoid getting other folders
      // Could also use getResources and iterate
      URL url =  cl.getResource(moduleId + File.separator + "package.json");
      if( url == null){
        url = cl.getResource(moduleId + File.separator + "index.json");
      }
      if( url != null) {
        url = new File(url.getFile()).getParentFile().toURI().toURL();
      } else {
        String resourceName = moduleId;
        if(!moduleId.endsWith(".js")){
          resourceName = moduleId + ".js";
        }
        url = cl.getResource(resourceName);
      }

      if(url != null) {
        URI uri = url.toURI();
        URI base = uri.resolve("");
        return loadFromUri(uri, base, validator);
      }
    }

    return super.loadFromPrivilegedLocations(moduleId, validator);

  }

  private ModuleSource loadAsFileOrDirectory(String moduleId, URI base) throws IOException {

    String modulePath = base.getPath() + File.separator + moduleId;

    String moduleFile = modulePath;
    if(!moduleFile.endsWith(".js")){
      moduleFile += ".js";
    }

    URI fileUri = new File(moduleFile).toURI();

    ModuleSource source = loadAsFile(fileUri);

    if(source != null) {
      return source;
    }

    URI directoryUri =  new File(modulePath).toURI();

    source = loadAsDirectory(directoryUri);

    if(source != null) {
      return source;
    }

    return null;
  }

  private URI getCurrentModuleURI(){
    ModuleScope currentScope = NodeRequire.moduleScope.get();
    if(currentScope == null) {
      return new File(".").toURI().resolve("");
    }
    return currentScope.getUri();
  }

  private ModuleSource loadNodeModules(String moduleId) throws IOException {

    URI uri = getCurrentModuleURI();

    //URI uri = new File(".").toURI().resolve("");
    ModuleSource source = null;

    while(source == null && !uri.getPath().equals("/")) {
      URI nodeModulesUri = uri.resolve("node_modules/");

      source = loadAsFileOrDirectory(moduleId, nodeModulesUri);

      uri = uri.resolve("..");
    }

    return source;
  }

  private ModuleSource loadAsDirectory(URI uri) throws IOException {
    if(!new File(uri).isDirectory()) {
      return null;
    }

    String main = "index.js";

    // Allow loading modules from <dir>/package.json
    File packageFile = new File(uri.getPath(), "package.json");

    if(packageFile.exists()){

      String conf = null;
      try {
        conf = new Scanner(packageFile).useDelimiter("\\A").next();
      } catch (FileNotFoundException e) {}

      JsonObject json;
      try {
        json = new JsonObject(conf);
      } catch (DecodeException e) {
        throw new IllegalStateException("Module " + uri.toString() + " package.json contains invalid json");
      }

      main = json.getString("main");

      if(!main.endsWith(".js")){
        main = main + ".js";
      }
    }

    // Allow loading modules from <dir>/<main>.js
    File mainFile = new File(uri.getPath(), main);
    return loadAsFile(mainFile.toURI());

  }

  protected ModuleSource loadAsFile(URI uri) throws IOException {
    if(!new File(uri).isFile()) {
      return null;
    }
    URI base = uri.resolve("");
    return loadFromActualUri(uri, base, null);
  }

  protected boolean isRelativeModule(String moduleId){
    return (moduleId.startsWith("./") || moduleId.startsWith("/") || moduleId.startsWith("../"));
  }

  @Override
  protected ModuleSource loadFromFallbackLocations(String moduleId, Object validator) throws IOException, URISyntaxException {
    if(isRelativeModule(moduleId)){
       URI current = getCurrentModuleURI();
      return loadAsFileOrDirectory(moduleId, current);
    }
    return loadNodeModules(moduleId);
  }

  @Override
  protected ModuleSource loadFromUri(URI uri, URI base, Object validator)
          throws IOException, URISyntaxException
  {

    File file = new File(uri);
    String moduleId = file.getName();

    if(base == null)
      base = file.getParentFile().toURI().resolve("");

    return loadAsFileOrDirectory(moduleId, base);

    //return loadFromActualUri(uri, base, validator);
  }
}
