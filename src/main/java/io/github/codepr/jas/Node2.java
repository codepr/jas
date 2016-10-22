package io.github.codepr.jas;

import java.rmi.RemoteException;
import io.github.codepr.jas.actors.ActorRef;
import io.github.codepr.jas.actors.ActorSystem.ActorMode;
import io.github.codepr.jas.actors.impl.ActorSystemImpl;
import io.github.codepr.jas.actors.remote.Cluster;
import io.github.codepr.jas.actors.remote.ClusterFactory;

public class Node2 {
    public static void main(String[] args) {
        Cluster cluster = ClusterFactory.joinCluster(new ActorSystemImpl(), "localhost");
        try {
            ActorRef ref3 = cluster.actorOf(TrivialActor.class, ActorMode.LOCAL, "127.0.0.1/ref3");
            ActorRef ref2 = cluster.actorSelection("127.0.0.1/ref2");
            ActorRef ref5 = cluster.actorSelection("127.0.0.1/ref5");
            ref3.send(new TrivialMessage(), ref2);
            // ref2.send(new TrivialMessage(), ref5);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
