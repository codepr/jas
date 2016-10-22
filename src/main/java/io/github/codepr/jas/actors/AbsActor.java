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

import java.rmi.RemoteException;
import io.github.codepr.jas.actors.exceptions.NoSuchActorException;
import io.github.codepr.jas.actors.exceptions.UnsupportedMessageException;
import io.github.codepr.jas.actors.mailbox.MailBox;
import io.github.codepr.jas.actors.mailbox.MailBoxImpl;
import io.github.codepr.jas.actors.impl.AbsActorRef;

/**
 * Defines common properties of all actors.
 *
 * @author Riccardo Cardin
 * @version 1.0
 * @since 1.0
 * @author Andrea Giacomo Baldan
 * @version 2.0
 * @since 1.0
 */
public abstract class AbsActor<T extends Message> implements Actor<T> {

    /**
     * Self-reference of the actor
     */
    protected ActorRef<T> self;

    /**
     * Sender of the current message
     */
    protected ActorRef<T> sender;

    /**
     * MailBox for incoming messages
     */
    protected MailBox<T> mailBox;

    /**
     * Actor internal status flag
     */
    private volatile boolean alive;

    /**
     * Looping to apply receive method on incoming messages status flag
     */
    private volatile boolean looping;

    public AbsActor() {
        this.mailBox = new MailBoxImpl<>();
        this.alive = true;
        this.looping = false;
    }

    /**
     * Sets the self-reference.
     *
     * @param self The reference to itself
     * @return The actor.
     */
    protected final Actor<T> setSelf(ActorRef<T> self) {
        this.self = self;
        return this;
    }

    /**
     * Sets the reference to the sender of the current message
     * @param sender The reference to the sender of the current message
     */
    public final void setSender(ActorRef<T> sender) {
        this.sender = sender;
    }

    /**
     * Enqueue incoming messages inside the mailbox
     * @param message The message to be stored
     * @throws NoSuchActorException if actor status is not alive
     */
    public synchronized void enqueue(T message) {
        if (!alive)
            throw new NoSuchActorException();
        mailBox.enqueue(message);
        if (!this.looping) start();
    }

    /**
     * Stops the actor from receiving incoming messages, process remaining messages
     * in the mailbox and sets {@code alive} to false
     */
    public synchronized void stop() {
        this.looping = false;
        while (!mailBox.isEmpty()) {
            try {
                receive(getNextMessage());
            } catch (NoSuchActorException | UnsupportedMessageException e) {
                e.printStackTrace();
            }
        }
        this.alive = false;
    }

    /**
     * Return the actor status
     * @return True if alive, otherwise false
     */
    private boolean isAlive() {
        return this.alive;
    }

    /**
     * Return the receiving loop status
     * @return True if the actor is looping, otherwise false
     */
    private boolean isLooping() { return this.looping; }

    /**
     * Remove the head message from the mailbox, ready to be processed
     * @throws NoSuchActorException if the actor is not alive (stopped)
     * @return the head message of the mailbox if there's any
     */
    private T getNextMessage() {
        if (!alive)
            throw new NoSuchActorException();
        return this.mailBox.remove();
    }

    /**
     * Starts the receiving loop for the actor, set it's status to alive and looping
     */
    private synchronized void start() {
        this.alive = true;
        this.looping = true;
        try {
            ((AbsActorRef<T>) self).startReceivingLoop(new ReceiveLoop());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runnable type, process all messages inside the mailbox for the full duration
     * of the alive status of the actor
     */
    private class ReceiveLoop implements Runnable {
        @Override
        public void run() {
            /**
             * loop conditions:
             * - actor must be alive
             * - actor has already started looping
             */
            while (isAlive() && isLooping()) {
                try {
                    receive(getNextMessage());
                } catch (NoSuchActorException | UnsupportedMessageException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
