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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A reference of an actor that allow to locate it in the actor system.
 * Using this reference it is possible to send a message among actors.
 *
 * @author Andrea Giacomo Baldan
 * @version 1.0
 * @since 1.0
 */
public interface ActorRef<T extends Message> extends Remote {

    /**
     * Return the name of the actor
     *
     * @return a {@code String} representing the name of the actor
     */
    String getName() throws RemoteException;

    /**
     * Sends a {@code message} to another actor
     *
     * @param message The message to send
     * @param to The actor to which sending the message
     */
    void send(T message, ActorRef<T> to) throws RemoteException;
}
