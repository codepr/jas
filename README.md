# Jas - A legacy implementation of an actor system

A system that abstract a simplified implementation of
the [actor model](https://en.wikipedia.org/wiki/Actor_model).Started as
a [university project](https://github.com/codepr/pcd-actors) for a concurrent
and distributed programming course, I proceeded to add some features like
support for remote actors and a basic cluster system based on legacy RMI
technology.

## Logical architecture

For an exhaustive explanation of the system's logical architecture please refer
to [pcd-actors](https://github.com/codepr/pcd-actors)'s README, at a local level
the structure remained more or less the same, except for some additions aimed to
handle the distributed part of the Actor System.

The basic structure has been enveloped on a fairly basic and still without
failure-detection cluster system featuring legacy java RMI, therefore it needs
an *rmi registry* running.

## Building

The `jas` project is configured as a [Maven](https://maven.apache.org/) project. In detail, it was generated using the following command line:

mvn archetype:generate -DarchetypeGroupId=io.github.codepr.jas.actors -DarchetypeArtifactId=pcd-actors -DarchetypeVersion=1.0-SNAPSHOT.

To build the actor system library use the following command

$ mvn package

The output library will be created by Maven inside the folder `target`, with name `pcd-actors.jar`.

To run the tests use the command

$ mvn test

## License

The MIT License (MIT)

Copyright (c) 2015 Riccardo Cardin

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
