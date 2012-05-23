# node.vert.x

This is a simple node.js compatibility layer for [vert.x](http://vertx.io)

## Building

Clone the repo into your vertx/mods directory

Initialize the submodules:
`git submodule update --init`

Copy build.properties.sample to build.properties

`ant`

## Running

Add node.vert.x/bin to your PATH

Go to your project directory (ex: ShareJS)

Run the node.vert.x mod:

`node.vert.x <arguments>`

Ex:

Running ShareJS

`node.vert.x bin/exampleserver.js -p 8080`

## Notes

All the node.js modules are copied from [node.js](https://github.com/joyent/node) which means copyright Joyent, Inc. and other Node contributors.
