# Jas - A legacy implementation of an actor system

A system that abstract a simplified implementation of
the [actor model](https://en.wikipedia.org/wiki/Actor_model).Started as
a [university project](https://github.com/codepr/pcd-actors) for a concurrent
and distributed programming course, I proceeded to add some features like
support for remote actors and a basic cluster system based on legacy RMI
technology.

## Logical architecture

### Actor
An actor belonging to the type `Actor` holds the "interface" of the actor. The interface of an actor is identified by
the message it can respond to. The actor interface is fully defined by the method

void receive(T message)

Messages received by an actor are not immediately processed. They must be placed inside a dedicated queue, called
**mail box**. Messages inside mail box have to be processed *asynchronously*, which means that the processing of a
message has not to block the receiving loop of other messages by the actor.

The implementation of the actor must optimize the use of synchronized threads to satisfy the above requirements.

An actor has an actor reference (see the below type `ActorRef`) to itself and to the sender of the current processed
message.

#### Unknown messages

In the simple implementation requested by `pcd-actors`, if an actor does not know how to respond to a particular message
type, an `UnsupportedMessageException` is thrown. This is not the standard behaviour of an actor model. In a full
implementation of an actor model it should be a responsibility of the user to decide which action to take with respect
to an unknown message.

Moreover, the policy that let us thrown an exception in response to an unknown message is possible because in
`pcd-actors` an actor cannot change its interface through time. Actually, throwing an exception will stop the actor,
making useless any possible change of interface.

### ActorRef

A reference to an actor (formally an `ActorRef`) is an abstraction of the model used to address actors. There are two
different modes to address actors:

* Local mode: the actor is running in the local machine
* Remote mode: the actor may be running in a remote machine

Using this abstraction a remote actor can be used as a local actor, simplify the
model of processing. In order to obtain access to the **Remote mode**, an *RMI registry*
must be started.

Once an instance of `ActorRef` was obtained, it is possible to send a messages to the corresponding actor using the
following method:

void send(T message, ActorRef to);

 Messages can be sent only among actors. No other type can send a message to an
actor.

#### Actor reference for testing purpose

For *testing purpose*, it is necessary to give the possibility to retrieve the `Actor` associated to a reference. For
this reason, among the `test` types it's present the class `TestActorRef`. This class is a
[*decorator*](http://www.slideshare.net/RiccardoCardin/design-pattern-strutturali) of the `ActorRef`
type, that adds a single method:

protected abstract Actor<T> getUnderlyingActor(ActorSystem system);

Using this method it is possible to retrieve the corresponding `Actor`. The above method **must be implemented**.

### Message

A `Message` is the piece of information that actor send among each others. Each message should be logically divided into
three parts:

* A *tag*, which represents the operation requested by the message
* A *target*, which represents the address of the actor receiving the message
* A *payload*, which may represent the data that have to be sent with the message

![Graphical representation of the structure of a message](http://www.math.unipd.it/~rcardin/pcd/pcd-actors/Message%20structure.png)

### Actor system
The actor system (`ActorSystem`) has the responsibility to maintain reference to each actor created. Using the actor
system should be the only way to build a new instance of an actor. The factory methods exposed by the `ActorSystem` type
are:

ActorRef<? extends Message> actorOf(Class<Actor<?>> actor);
ActorRef<? extends Message> actorOf(Class<Actor<?>> actor, ActorMode mode);

The former lets to build a local instance of an actor of the given type. The latter lets to decide if a local instance
or a remote instance has to be built.

The actor system maintain the relationship between each actor and its reference, using a map. The map is indexed by
`ActorRef` and it is located inside the `AbsActorSystem` type. Accesses to the map have to be properly synchronized.

The actor system has also the responsibility to stop an actor and to stop the entire system, using the following
methods:

void stop();
void stop(ActorRef<?> actor);

Stopping an actor means that it cannot receive any message after the stopping operation. This operation must be accomplished
*gracefully*, which means that an actor has to process the messages that are already present in the mailbox before
stopping.

Trying to do any operation on a stopped actor must rise an `NoSuchActorException`. An actually stopped actor
should be eligible for garbage collection by the JVM an no thread should be associated to it anymore.

The `stop` method stops all the actors that are active in the actor system. Every actor has to be stopped *gracefully*,
as stated in above sentences.

#### Singleton view of the actor system

The actor system MUST have a single active instance. This instance have to be necessarily initialized in the `main`
method of the program.

In order to implement correctly the remote system, this instance have to be serializable. The best way to achieve this
functionality is to use a *dependence injection* framework, such as [Google Guice](https://github.com/google/guice),
[Spring](http://projects.spring.io/spring-framework/) or [CDI](http://docs.oracle.com/javaee/6/tutorial/doc/giwhl.html).
However, the use of an DI framework is far beyond the scopes of this little project.

So, the above property must be fulfilled using other techniques, that do not use explicitly any form of design pattern
Singleton

### Type's interactions

This section shows how the above types interact with each other to fulfill the relative functionality.

#### Actor creation
To create a new actor, ask the actor system to do the dirty job.

![Actor creation](http://www.math.unipd.it/~rcardin/pcd/pcd-actors/Actor%20creation.png)

So, first of all, a client must obtain a reference to the actor system. Using this reference, it asks the system to
create an new instance of an actor. The result of this request is the actor reference to the actor.

#### Message sending

Once a client have obtained the references to two actors it can ask the first to send a message to the second. Clearly,
to obtain the real instance of an actor (not its actor reference) the actor system must be queried.

![Message sending](http://www.math.unipd.it/~rcardin/pcd/pcd-actors/Message%20sending_1.png)

Clearly, the `ActorRef` cannot be directly responsible of the `receive` method call on an `Actor`. The responsibility of
an `ActorRef` is managing to let a `Message` to be put inside the `Actor`'s mailbox.

Most of time, the client will be an actor itself, that ask to the self reference to send a message to another actor.

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
