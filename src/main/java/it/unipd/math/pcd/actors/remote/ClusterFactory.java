package it.unipd.math.pcd.actors.remote;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.net.UnknownHostException;
import it.unipd.math.pcd.actors.ActorSystem;

public class ClusterFactory {

    public static Cluster startCluster(ActorSystem system, String seedHost) {
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
