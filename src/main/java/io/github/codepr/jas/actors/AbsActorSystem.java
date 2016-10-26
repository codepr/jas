/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Riccardo Cardin, Andrea Giacomo Baldan
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
package io.github.codepr.jas.actors;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import io.github.codepr.jas.actors.exceptions.NoSuchActorException;

/**
 * A map-based implementation of the actor system.
 *
 * @author Riccardo Cardin
 * @version 1.0
 * @since 1.0
 * @author Andrea Giacomo Baldan
 * @version 2.0
 * @since 1.0
 */
public abstract class AbsActorSystem implements ActorSystem {

    /**
     * Associates every Actor created with an identifier.
     */
    private Map<String, Actor<? extends Message>> actors;
    /**
     * Associates every remote name to the remote Actor identified by
     */
    private Map<String, ActorRef<? extends Message>> remoteActors;
    /**
     * {@code ActorSystem} mode, can be either {@code DEFAULT} to run on a
     * single machine, or {@code CLUSTER} to run on a cluster of multiple nodes.
     */
    private final SystemMode systemMode;

    public AbsActorSystem() {
        this.actors = new ConcurrentHashMap<>();
        this.remoteActors = new ConcurrentHashMap<>();
        this.systemMode = SystemMode.DEFAULT;
    }

    public AbsActorSystem(SystemMode systemMode) {
        this.actors = new ConcurrentHashMap<>();
        this.remoteActors = new ConcurrentHashMap<>();
        this.systemMode = systemMode;
    }

    /**
     * Return the {@code SystemMode} of the current {@code ActorSystem}
     */
    public final SystemMode getSystemMode() {
        return this.systemMode;
    }

    /**
     * Return the remote actors {@code Map} tracking all remote reference of
     * {@code ActorRef}.
     *
     * @return A {@code Map<String, ActorRef<?>>} containing all remote
     * reference of the cluster.
     */
    @Override
    public Map<String, ActorRef<? extends Message>> getRemoteActors() {
        return this.remoteActors;
    }

    /**
     * Create an instance of {@code actor} returning a {@link ActorRef reference}
     * to it using the given {@code mode} and a name.
     *
     * @param actor The type of actor that has to be created
     * @param mode The mode of the actor requested
     * @param name The name of the actor inside the cluster, must be unique
     *
     * @return A reference to the actor
     */
    @Override
    public ActorRef<? extends Message> actorOf(Class<? extends Actor> actor, ActorMode mode, String name) {
        // ActorRef instance
        ActorRef<? extends Message> reference;
        try {
            // Create the reference to the actor
            reference = this.createActorReference(mode, name);
            // Create the new instance of the actor
            Actor<? extends Message> actorInstance =
                ((AbsActor) actor.newInstance()).setSelf(reference);
            // Associate the reference to the actor
            if (mode == ActorMode.LOCAL)
                actors.put(name, actorInstance);
            else remoteActors.put(name, reference);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new NoSuchActorException(e);
        }
        return reference;
    }

    /**
     * Create an instance of {@code actor} returning a {@link ActorRef reference}
     * to it using the given {@code mode}, generating a UUID as name.
     *
     * @param actor The type of actor that has to be created
     * @param mode The mode of the actor requested
     *
     * @return A reference to the actor
     */
    @Override
    public ActorRef<? extends Message> actorOf(Class<? extends Actor> actor, ActorMode mode) {
        // default to localhost
        String host = "127.0.0.1";
        if (mode == ActorMode.REMOTE) {
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return actorOf(actor, mode, host + "/" + UUID.randomUUID().toString());
        } else return actorOf(actor, mode, UUID.randomUUID().toString());
    }

    /**
     * Create an instance of {@code actor} that executes locally.
     *
     * @param actor The type of actor that has to be created
     * @return A reference to the actor
     */
    @Override
    public ActorRef<? extends Message> actorOf(Class<? extends Actor> actor) {
        return this.actorOf(actor, ActorMode.LOCAL);
    }

    /**
     * Stops {@code actor}.
     *
     * @param actor The actor to be stopped
     */
    @Override
    public void stop(ActorRef<? extends Message> actor) {
        String name = "";
        try {
            name = actor.getName();
        } catch (RemoteException e) {}
        if (!actors.containsKey(name)) {
            throw new NoSuchActorException();
        }
        ((AbsActor<? extends Message>) actors.get(name)).stop();
        actors.remove(name);
    }

    /**
     * Stops all actors of the system.
     */
    @Override
    public void stop() {
        actors.entrySet()
            .stream()
            .forEach(x -> ((AbsActor<? extends Message>) x.getValue()).stop());
        actors.clear();
    }

    /**
     * Check if the current {@code ActorSystem} contains a given {@code ActorRef}
     */
    public boolean contains(ActorRef<? extends Message> actorRef) {
        String name = "";
        try {
            name = actorRef.getName();
        } catch (RemoteException e) {}
        return (actors.containsKey(name)) ? true : false;
    }

    /**
     * Check if the current {@code ActorSystem} contains a given
     * remote {@code ActorRef}
     */
    public boolean containsRemote(String name) {
        return (remoteActors.containsKey(name)) ? true : false;
    }

    /**
     * Add a remote {@code ActorRef} to the current {@code ActorSystem}
     */
    public void addRemoteRef(String name, ActorRef<? extends Message> remoteRef) {
        remoteActors.put(name, remoteRef);
    }

    /**
     * Return the actor associated to a given ActorRef inside the HashMap
     *
     * @param ref reference to ActorRef
     * @return The actor associated to ref
     * @throws NoSuchActorException if no actor was found
     */
    public Actor<? extends Message> getActor(ActorRef<? extends Message> ref) {
        Actor<? extends Message> ret = null;
        try {
            ret = actors.get(ref.getName());
        } catch (RemoteException e) {}
        if (ret == null) throw new NoSuchActorException();
        return ret;
    }

    /**
     * Execute a runnable with {@code eService} instance of Executor, used to
     * start the consuming loop of every actor's mailbox.
     *
     * @param receivingLoop Runnable type to be executed
     */
    public abstract void startActorRunnable(Runnable actorRunnable);

    /**
     * Create an instance of {@link ActorRef}
     *
     * @param mode Possible mode to create an actor. Could be{@code LOCAL} or
     * {@code REMOTE}.
     * @param name A String representing the name of the Actor inside the Cluster
     * must be unique
     *
     * @return An instance to {@link ActorRef}
     */
    protected abstract ActorRef<? extends Message> createActorReference(ActorMode mode, String name);

}
