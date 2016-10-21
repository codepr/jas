package it.unipd.math.pcd;

import java.rmi.RemoteException;
import it.unipd.math.pcd.actors.ActorRef;
import it.unipd.math.pcd.actors.ActorSystem.ActorMode;
import it.unipd.math.pcd.actors.impl.ActorSystemImpl;
import it.unipd.math.pcd.actors.remote.Cluster;
import it.unipd.math.pcd.actors.remote.ClusterFactory;

public class Main {
    public static void main(String[] args) {
        Cluster cluster = ClusterFactory.startCluster(new ActorSystemImpl(), "127.0.0.1");
        try {
            ActorRef ref1 = cluster.actorOf(TrivialActor.class, ActorMode.LOCAL, "127.0.0.1/ref1");
            ActorRef ref2 = cluster.actorOf(TrivialActor.class, ActorMode.REMOTE, "127.0.0.1/ref2");
            ref1.send(new TrivialMessage(), ref2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
