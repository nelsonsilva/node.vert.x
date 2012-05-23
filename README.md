# node.vert.x

This is a simple node.js compatibility layer for [vert.x](http://vertx.io)

## Building

Clone the repo into your vertx/mods directory

Copy build.properties.sample to build.properties

`ant`

## Running

Go to your project directory (ex: ShareJS)

Create a configuration file, ex (vertx.json) :

`
{
  "main": "bin/exampleserver"
}
`

Run the node.vert.x mod and pass it the conf file:

`vertx run node.vert.x -conf vertx.json`

## Notes

All the node.js modules are copied from [node.js](https://github.com/joyent/node) which means copyright Joyent, Inc. and other Node contributors.
