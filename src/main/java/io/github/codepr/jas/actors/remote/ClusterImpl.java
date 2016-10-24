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
package io.github.codepr.jas.actors.remote;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import io.github.codepr.jas.actors.AbsActorSystem;
import io.github.codepr.jas.actors.Actor;
import io.github.codepr.jas.actors.ActorRef;
import io.github.codepr.jas.actors.ActorSystem;
import io.github.codepr.jas.actors.ActorSystem.ActorMode;
import io.github.codepr.jas.actors.exceptions.NoSuchActorException;
import io.github.codepr.jas.actors.AbsActorRef;

/**
 * Basic cluster implementation, handle an {@code ActorSystem} for every
 * instance and tracks every new member added, currently there's no leader
 * election system nor failure detection across the cluster.
 * Could be possibly interesting to implement a basic heartbeat system, maybe a
 * gossip protocol like Akka's one.
 *
 * @author Andrea Giacomo Baldan
 * @version 2.0
 * @since 1.0
 */
public class ClusterImpl extends UnicastRemoteObject implements Cluster {

    // public static final long serialVersionUID = 227L;
    /**
     * A unique id inside the cluster, must be unique to be registered in the
     * RMI registry
     */
    private final String uuid;

    /**
     * Seed host, can be seen as a sorta of leader
     */
    private final String seed;

    /**
     * Reference to the local {@code ActorSystem}
     */
    private final ActorSystem system;

    /**
     * Members of the cluster, tracks all new joining members
     */
    private Set<String> members;

    public ClusterImpl(ActorSystem system, String host, String seedHost) throws RemoteException {
        super();
        this.system = (AbsActorSystem) system;
        this.members = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        this.seed = seedHost;
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
        System.out.println(" [*] New member added to the cluster: rmi://" + name);
    }

    @Override
    public ActorRef actorOf(Class<? extends Actor> actor, ActorMode mode, String name) throws RemoteException {
        if (mode == ActorMode.LOCAL) {
            ActorRef<?> localRef = system.actorOf(actor, mode, name);
            updateRemoteActors(name, localRef);
            return localRef;
        } else {
            ActorRef<?> remoteRef = null;
            String addr = name.split("/")[0];
            String memberName = (String)
                members.stream().filter(x -> x.startsWith(addr)).toArray()[0];
            try {
                Cluster remoteMember = (Cluster) Naming.lookup("rmi://" + memberName);
                remoteRef = remoteMember.actorOf(actor, ActorMode.LOCAL, name);
                String id = remoteRef.getName();
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }
            return remoteRef;
        }
    }

    @Override
    public ActorRef actorSelection(String address) throws RemoteException {
        ActorRef remoteRef = null;
        try {
            remoteRef = (ActorRef<?>) Naming.lookup("rmi://" + address);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            System.out.println(" [!] No remote actor found " + e.getMessage());
            e.printStackTrace();
        }
        return remoteRef;
    }

    @Override
    public void addRemoteRef(String name, ActorRef<?> remoteRef) throws RemoteException {
        ((AbsActorSystem) system).addRemoteRef(name, remoteRef);
        System.out.println(" [*] New remote actor added to the cluster: rmi://" + name);
    }

    /**
     * Update members {@code Set} of every member of the cluster
     *
     * @param seedHost The seed node used to start and join the cluster, can be
     * seen as a sort of leader in the cluster.
     * @param memberName The new joining member unique name inside the cluster
     */
    private void updateMembers(String seedHost, String memberName) {
        try {
            final Cluster cluster = (Cluster) Naming.lookup("rmi://" + seedHost + "/master");
            cluster.join(memberName);
            // update remote actors to the newest joined node
            cluster.updateRemoteActors(memberName);
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

    @Override
    public void updateRemoteActors(String newMember) throws RemoteException {
        system.getRemoteActors()
            .entrySet()
            .stream()
            .forEach(x -> {
                    try {
                        final Cluster cluster =
                            (Cluster) Naming.lookup("rmi://" + newMember);
                        cluster.addRemoteRef(x.getKey(), x.getValue());
                    } catch (RemoteException | MalformedURLException | NotBoundException e) {
                        e. printStackTrace();
                    }
                });
    }

    @Override
    public void stop(ActorRef<?> actorRef) throws RemoteException {
        AbsActorSystem abSystem = (AbsActorSystem) system;
        if (!abSystem.contains(actorRef)) {
            String destName = actorRef.getName();
            if (abSystem.containsRemote(destName)) {
                try {
                    String addr = destName.split("/")[0];
                    String memberName = (String)
                        members.stream().filter(x -> x.startsWith(addr)).toArray()[0];
                    Cluster clusterMember = (Cluster) Naming.lookup("rmi://" + memberName);
                    clusterMember.stop(actorRef);
                } catch (NotBoundException | MalformedURLException e) {
                    e.printStackTrace();
                }
            } else throw new NoSuchActorException();
        } else {
            system.stop(actorRef);
        }
    }

    @Override
    public void stop() throws RemoteException {
        members.stream()
            .forEach(x -> {
                    try {
                        final Cluster clusterMember =
                            (Cluster) Naming.lookup("rmi://" + x);
                        clusterMember.stopSystem();
                    } catch (RemoteException | NotBoundException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void stopSystem() throws RemoteException {
        system.stop();
    }

    /**
     * Update remote {@code ActorRef} tracker inside the {@code ActorSystem} of
     * every member of the cluster
     *
     * @param name The unique name of the remote {@code ActorRef} created inside
     * the cluster
     * @param remoteRef The {@code ActorRef} remote reference to be tracked
     * inside every instance of {@code ActorSystem} inside the cluster
     */
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
        try {
            final Cluster master = (Cluster) Naming.lookup("rmi://" + this.seed + "/master");
            master.addRemoteRef(name, remoteRef);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }
    }
}
