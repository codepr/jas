package io.github.codepr.jas.actors.remote;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.net.UnknownHostException;
import io.github.codepr.jas.actors.ActorSystem;

public class ClusterFactory {

    public static final Cluster startCluster(ActorSystem system, String seedHost) {
        Cluster cluster = null;
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            cluster = new ClusterImpl(system, host, seedHost);
        } catch (UnknownHostException | RemoteException e) {
            e.printStackTrace();
        }
        return cluster;
    }

}
