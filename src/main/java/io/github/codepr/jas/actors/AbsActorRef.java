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
package io.github.codepr.jas.actors;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import io.github.codepr.jas.actors.ActorSystem;
import io.github.codepr.jas.actors.ActorSystem.SystemMode;
import io.github.codepr.jas.actors.ActorSystem.ActorMode;
import io.github.codepr.jas.actors.AbsActorSystem;
import io.github.codepr.jas.actors.ActorRef;
import io.github.codepr.jas.actors.AbsActor;
import io.github.codepr.jas.actors.Message;
import io.github.codepr.jas.actors.exceptions.NoSuchActorException;

/**
 * A reference of an actor that allow to locate it in the actor system.
 * Using this reference it is possible to send a message among actors.
 *
 * @author Andrea Giacomo Baldan
 * @version 2.0
 * @since 1.0
 */
public abstract class AbsActorRef<T extends Message> extends UnicastRemoteObject implements ActorRef<T> {

    /**
     * Reference to the {@code system}
     */
    protected final AbsActorSystem system;
    /**
     * Name of the {@code ActorRef} bound to the registry
     */
    protected final String name;
    /**
     * {@code ActorRef} reference representing the original sender of the
     * {@code Message} in case of remote communication
     */
    protected ActorRef<T> originalSender;

    /**
     * Public constructor, in case of an {@code ActorRef} of type
     * {@code ActorMode.REMOTE} bind the name to the RMI registry.
     */
    public AbsActorRef(ActorSystem system, ActorMode mode, String name) throws RemoteException {
        super();
        this.system = (AbsActorSystem) system;
        this.name = name;
        this.originalSender = this;
        if (this.system.getSystemMode() == SystemMode.CLUSTER) {
            try {
                Naming.bind("rmi://" + name, this);
            } catch (AlreadyBoundException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set the {@code ActorRef} reference representing the original sender of
     * the {@code Message} in case of remote {@code ActorRef}.
     */
    @Override
    public void setOriginalSender(ActorRef<T> originalSender) throws RemoteException {
        this.originalSender = originalSender;
    }

    /**
     * Return the {@code ActorRef} reference of the original sender of the
     * {@code Message} in case of remote {@code ActorRef}.
     */
    @Override
    public ActorRef<T> getOriginalSender() throws RemoteException {
        return this.originalSender;
    }

    /**
     * Return the name of the actor
     *
     * @return a {@code String} representing the name of the actor
     */
    @Override
    public final String getName() throws RemoteException { return this.name; }

    /**
     * Sends a {@code message} to another actor
     *
     * @param message The message to send
     * @param to The actor to which sending the message
     */
    @Override
    public void send(T message, ActorRef<T> to) throws RemoteException {
        if (!system.contains(to)) {
            String destName = to.getName();
            if (system.containsRemote(destName)) {
                try {
                    String realDest = to.getOriginalSender().getName();
                    System.out.println(" [" + originalSender.getName() + "] sending message to remote actor: " + realDest);
                    ActorRef<T> remoteRef = (ActorRef<T>) Naming.lookup("rmi://" + destName);
                    remoteRef.setOriginalSender(this);
                    remoteRef.send(message, to);
                } catch (NotBoundException | MalformedURLException e) {
                    e.printStackTrace();
                }
            } else throw new NoSuchActorException();
        } else {
            try {
                if (originalSender == this)
                    ((AbsActor<T>) system.getActor(to)).setSender(this);
                else
                    ((AbsActor<T>) system.getActor(to)).setSender(this.originalSender);
                ((AbsActor<T>) system.getActor(to)).enqueue(message);
            } catch (NoSuchActorException e) {
                throw e;
            }
        }
    }

    /**
     * Execute a runnable using {@code system}
     * @param receivingLoop Runnable type to be executed
     */
    public void startReceivingLoop(Runnable receivingLoop) throws RemoteException {
        system.startActorReceiveLoop(receivingLoop);
    }
}
