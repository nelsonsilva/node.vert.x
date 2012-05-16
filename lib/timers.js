this.setTimeout = function(fn, delay) {
    return vertx.setTimer(delay, fn);
};
this.clearTimeout = function(id) {
    if (id != null) return vertx.cancelTimer(id);
};