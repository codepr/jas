/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Andrea Giacomo Baldan
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 */
package it.unipd.math.pcd.actors.remote;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import it.unipd.math.pcd.actors.AbsActorSystem;
import it.unipd.math.pcd.actors.Actor;
import it.unipd.math.pcd.actors.ActorRef;
import it.unipd.math.pcd.actors.ActorSystem;
import it.unipd.math.pcd.actors.ActorSystem.ActorMode;
import it.unipd.math.pcd.actors.impl.AbsActorRef;

public class ClusterImpl extends UnicastRemoteObject implements Cluster {

    // public static final long serialVersionUID = 227L;
    private final String uuid;
    private final ActorSystem system;
    private Set<String> members;

    public ClusterImpl(ActorSystem system, String host, String seedHost) throws RemoteException {
        super();
        this.system = (AbsActorSystem) system;
        this.members = new HashSet<String>();
        System.out.println("Host: " + host + " seed: " + seedHost);
        if (!host.equals(seedHost))
            this.uuid = host + "/" + UUID.randomUUID().toString().replaceAll("-", "");
        else this.uuid = host + "/master";
        try {
            Naming.bind("rmi://" + this.uuid, this);
            join(this.uuid);
            if (!host.equals(seedHost))
                updateMembers(seedHost, this.uuid);
        } catch (AlreadyBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void join(String name) throws RemoteException {
        members.add(name);
        System.out.println(" *** New member added to the cluster: rmi://" + name + " *** ");
    }

    @Override
    public ActorRef actorOf(Class<? extends Actor> actor, ActorMode mode, String name) throws RemoteException {
        if (mode == ActorMode.LOCAL)
            return system.actorOf(actor, mode, name);
        else {
            ActorRef remoteRef = system.actorOf(actor, ActorMode.LOCAL, name);
            String id = ((AbsActorRef<?>) remoteRef).getName();
            addRemoteRef(id, remoteRef);
            updateRemoteActors(this.uuid + id, remoteRef);
            return remoteRef;
        }
    }

    @Override
    public void addRemoteRef(String name, ActorRef<?> remoteRef) throws RemoteException {
        ((AbsActorSystem) system).addRemoteRef(name, remoteRef);
        System.out.println(" *** New remote actor added to the cluster: rmi://" + name + " *** ");
    }

    private void updateMembers(String seedHost, String memberName) {
        try {
            final Cluster cluster = (Cluster) Naming.lookup("rmi://" + seedHost + "/master");
            cluster.join(memberName);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }
        // update all bounded members
        members.stream()
            .filter(x -> !x.equals(uuid))
            .forEach(x -> {
                    try {
                        final Cluster clusterMember = (Cluster) Naming.lookup("rmi://" + x);
                        clusterMember.join(memberName);
                    } catch (NotBoundException | MalformedURLException | RemoteException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void updateRemoteActors(String name, ActorRef<?> remoteRef) {
        members.stream()
            .filter(x -> !x.equals(uuid))
            .forEach(x -> {
                    try {
                        final Cluster clusterMember = (Cluster) Naming.lookup("rmi://" + x);
                        clusterMember.addRemoteRef(name, remoteRef);
                    } catch (NotBoundException | MalformedURLException | RemoteException e) {
                        e.printStackTrace();
                    }
                });
    }
}
