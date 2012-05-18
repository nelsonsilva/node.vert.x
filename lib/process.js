this.process = {

  nextTick: function(fn) {
    return vertx.setTimer(0, fn);
  },

  env: {},

  on: function(event, handler){}
};
