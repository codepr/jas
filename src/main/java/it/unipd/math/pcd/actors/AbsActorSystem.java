/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 Riccardo Cardin
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
package it.unipd.math.pcd.actors;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import it.unipd.math.pcd.actors.exceptions.NoSuchActorException;

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
    private Map<ActorRef<?>, Actor<?>> actors;
    /**
     * Associates every remote name to the remote Actor identified by
     */
    private Map<String, ActorRef<?>> remoteActors;

    public AbsActorSystem() {
        actors = new ConcurrentHashMap<>();
        remoteActors = new ConcurrentHashMap<>();
    }

    @Override
    public ActorRef<? extends Message> actorOf(Class<? extends Actor> actor, ActorMode mode, String name) {

        // ActorRef instance
        ActorRef<?> reference;
        try {
            // Create the reference to the actor
            reference = this.createActorReference(mode, name);
            // Create the new instance of the actor
            Actor actorInstance = ((AbsActor) actor.newInstance()).setSelf(reference);
            // Associate the reference to the actor
            if (mode == ActorMode.LOCAL)
                actors.put(reference, actorInstance);
            else remoteActors.put(name, reference);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new NoSuchActorException(e);
        }
        return reference;
    }

    @Override
    public ActorRef<? extends Message> actorOf(Class<? extends Actor> actor, ActorMode mode) {
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

    @Override
    public ActorRef<? extends Message> actorOf(Class<? extends Actor> actor) {
        return this.actorOf(actor, ActorMode.LOCAL);
    }

    @Override
    public void stop(ActorRef<?> actor) {
        if (!actors.containsKey(actor)) {
            throw new NoSuchActorException();
        }
        ((AbsActor) actors.get(actor)).stop();
        actors.remove(actor);
    }

    @Override
    public void stop() {
        actors.entrySet()
            .stream()
            .forEach(x -> ((AbsActor<?>) x.getValue()).stop());
        actors.clear();
    }

    public boolean contains(ActorRef<?> actorRef) {
        return (actors.containsKey(actorRef)) ? true : false;
    }

    public boolean containsRemote(String name) {
        return (remoteActors.containsKey(name)) ? true : false;
    }

    public void addRemoteRef(String name, ActorRef<?> remoteRef) {
        remoteActors.put(name, remoteRef);
    }

    /**
     * Return the actor associated to a given ActorRef inside the HashMap
     * @param ref reference to ActorRef
     * @return The actor associated to ref
     * @throws NoSuchActorException if no actor was found
     */
    public Actor<?> getActor(ActorRef<?> ref) {
        Actor ret = actors.get(ref);
        if (ret == null) throw new NoSuchActorException();
        return ret;
    }

    /**
     * Execute a runnable with {@code eService} instance of Executor
     * @param receivingLoop Runnable type to be executed
     */
    public abstract void startActorReceiveLoop(Runnable receivingLoop);

    /**
     * Create an instance of {@link ActorRef}
     * @param mode Possible mode to create an actor. Could be{@code LOCAL} or
     * {@code REMOTE}.
     * @param name A String representing the name of the Actor inside the Cluster
     * must be unique
     *
     * @return An instance to {@link ActorRef}
     */
    protected abstract ActorRef createActorReference(ActorMode mode, String name);
}
