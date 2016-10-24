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

### General operations

In order to start some actors it is necessary to create an actor system and get
from it actor reference, used to send messages between actors.

#### Local

Currently there's no real distinction between local actors and remote actors, so
for now it is necessary to catch `RemoteException` or declare it in the `throws`
clausole.
Here an example using a basic implementation of an actor:

A basic `message` implementation

```java
import io.github.codepr.jas.actors.Message;

public class TrivialMessage implements Message {}
```

An actor that do nothing usefull on the `receive`, just printing who sent the
message

```java
import java.io.Serializable;
import java.rmi.RemoteException;
import io.github.codepr.jas.actors.AbsActor;

public class TrivialActor extends AbsActor<TrivialMessage> implements Serializable {
    private static final long serialVersionUID = 227L;

    @Override
    public void receive(TrivialMessage message) {
        try {
            System.out.println(" [" + self.getName() + "] Trivial message received from " + sender.getName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
```

The `ActorSystem` running

```java
import java.rmi.RemoteException;
import io.github.codepr.jas.actors.ActorRef;
import io.github.codepr.jas.actors.ActorSystem.ActorMode;
import io.github.codepr.jas.actors.ActorSystemImpl;

public class Main {
    public static void main(String[] args) {
        ActorSystem system = new ActorSystemImpl();
        try {
            ActorRef ref1 = system.actorOf(TrivialActor.class, ActorMode.LOCAL, "127.0.0.1/ref1");
            ActorRef ref2 = system.actorOf(TrivialActor.class, ActorMode.LOCAL, "127.0.0.1/ref2");
            ActorRef ref5 = system.actorOf(TrivialActor.class, ActorMode.LOCAL, "127.0.0.1/ref5");
            ref1.send(new TrivialMessage(), ref2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
```

#### Cluster Mode

**Node 1**

```java
import java.rmi.RemoteException;
import io.github.codepr.jas.actors.ActorRef;
import io.github.codepr.jas.actors.ActorSystem.ActorMode;
import io.github.codepr.jas.actors.ActorSystem.SystemMode;
import io.github.codepr.jas.actors.ActorSystemImpl;
import io.github.codepr.jas.actors.remote.Cluster;
import io.github.codepr.jas.actors.remote.ClusterFactory;

public class Node1 {
    public static void main(String[] args) {
        // ActorSystemImpl takes at least 1 argument, here we start it in CLUSTER mode, setting the host as 10.0.0.1
        // and the seed host (can be seen as a leader needed to start and join the cluster) to the same address.
        // If omitted those fields default to 127.0.0.1
        Cluster cluster = ClusterFactory.joinCluster(new ActorSystemImpl(SystemMode.CLUSTER), "10.0.0.1", "10.0.0.1");
        try {
            ActorRef ref1 = cluster.actorOf(TrivialActor.class, ActorMode.LOCAL, "10.0.0.1/ref1");
            ActorRef ref2 = cluster.actorOf(TrivialActor.class, ActorMode.REMOTE, "10.0.0.1/ref2");
            ActorRef ref5 = cluster.actorOf(TrivialActor.class, ActorMode.REMOTE, "10.0.0.1/ref5");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
```

**Node 2**

```java
import java.rmi.RemoteException;
import io.github.codepr.jas.actors.ActorRef;
import io.github.codepr.jas.actors.ActorSystem.ActorMode;
import io.github.codepr.jas.actors.ActorSystem.SystemMode;
import io.github.codepr.jas.actors.ActorSystemImpl;
import io.github.codepr.jas.actors.remote.Cluster;
import io.github.codepr.jas.actors.remote.ClusterFactory;

public class Node2 {
    public static void main(String[] args) {
        Cluster cluster = ClusterFactory.joinCluster(new ActorSystemImpl(SystemMode.CLUSTER), "10.0.0.2", "10.0.0.1");
        try {
            ActorRef ref3 = cluster.actorOf(TrivialActor.class, ActorMode.LOCAL, "10.0.0.2/ref3");
            ActorRef ref2 = cluster.actorSelection("10.0.0.1/ref2");
            ActorRef ref5 = cluster.actorSelection("10.0.0.1/ref5");
            ref3.send(new TrivialMessage(), ref2);
            ref2.send(new TrivialMessage(), ref5);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
```

## Building

The `jas` project is configured as a [Maven](https://maven.apache.org/) project. In detail, it was generated using the following command line:

mvn archetype:generate -DarchetypeGroupId=io.github.codepr.jas.actors -DarchetypeArtifactId=pcd-actors -DarchetypeVersion=1.0-SNAPSHOT.

To build the actor system library use the following command

$ mvn package

The output library will be created by Maven inside the folder `target`, with name `pcd-actors.jar`.

To run the tests use the command

$ mvn test

## TODO

* Some refactoring of bad code parts
* Better handle of the `RemoteException` thing, specially for local actors
* A basic failure detection on the cluster side of the project

## License

The MIT License (MIT)

Copyright (c) 2016 Andrea Giacomo Baldan

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
