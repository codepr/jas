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

import java.rmi.Remote;
import java.rmi.RemoteException;
import io.github.codepr.jas.actors.Actor;
import io.github.codepr.jas.actors.ActorRef;
import io.github.codepr.jas.actors.ActorSystem.ActorMode;

/**
 * Basic cluster, it makes possible to run multiple {@code ActorSystem} across a
 * network of computers and instantiation of remote Actors.
 *
 * @author Andrea Giacomo Baldan
 * @version 2.0
 * @since 1.0
 */
public interface Cluster extends Remote {

    /**
     * Join the cluster by adding the requester name to the concurrent set used
     * to track members inside the cluster.
     *
     * @param name A String representing the name of the new member to be added
     */
    void join(String name) throws RemoteException;

    /**
     * Add a remote {@code ActorRef} reference to the system associated to the
     * current node inside the cluster, in order to make aware of Remote Actors
     * all members of the cluster.
     *
     * @param name A String representing the name of the {@code ActorRef} inside
     * the cluster
     * @param remoteRef a {@code ActorRef} reference representing the remote
     * actor
     */
    void addRemoteRef(String name, ActorRef<?> remoteRef) throws RemoteException;

    /**
     * Create an instance of {@code actor} returning a {@link ActorRef reference}
     * to it using the given {@code mode} and a unique name inside the cluster.
     *
     * @param actor The type of actor that has to be created
     * @param mode The mode of the actor requested
     * @param name The name of the actor inside the cluster, must be unique
     *
     * @return A reference to the actor
     */
    ActorRef actorOf(Class<? extends Actor> actor, ActorMode mode, String name) throws RemoteException;
}
