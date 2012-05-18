exports.readFileSync = function(filename, encoding) {
    vertx.fileSystem.readFileSync(filename);
}

exports.readFile = function(filename, handler) {
    vertx.fileSystem.readFile(filename, handler);
}

/* TODO
vertx.fileSystem.copy(source, destination, handler)
vertx.fileSystem.move(source, destination, handler);
vertx.fileSystem.truncate(file, len, handler);
vertx.fileSystem.chmod(file, perms, handler);
vertx.fileSystem.props(file, handler);
vertx.fileSystem.lprops(file, handler);
vertx.fileSystem.link(link, existing, handler);
vertx.fileSystem.symlink(link, existing, handler);
vertx.fileSystem.unlink(link, handler);
vertx.fileSystem.readSymLink(link, handler);
vertx.fileSystem.delete(file, handler);
vertx.fileSystem.mkdir(dirname, handler);
vertx.fileSystem.readDir(dirName, filter);
vertx.fileSystem.readFile(file);
vertx.fileSystem.writeFile(file, data, handler);
vertx.fileSystem.createFile(file, handler);
vertx.fileSystem.exists(file, handler);
vertx.fileSystem.fsProps(file, handler);
vertx.fileSystem.open(file, openFlags, handler);
*/


