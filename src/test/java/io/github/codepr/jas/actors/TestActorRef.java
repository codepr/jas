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
import io.github.codepr.jas.actors.*;

/**
 * Decorates an {@link ActorRef} adding the ability to get the underlying actor associated to the reference.
 *
 * @author Riccardo Cardin
 * @version 1.0
 * @since 1.0
 * @author Andrea Giacomo Baldan
 * @version 2.0
 * @since 1.0
 */
public class TestActorRef<T extends Message> implements ActorRef<T> {

    private ActorRef<T> reference;

    public TestActorRef(ActorRef<T> actorRef) {
        this.reference = actorRef;
    }

    /**
     * Returns the {@link Actor} associated to the internal reference.
     * @param system Actor system from which retrieving the actor
     *
     * @return An actor
     */
    public Actor<T> getUnderlyingActor(ActorSystem system) {
        return (Actor<T>)((AbsActorSystem) system).getActor(reference);
    }

    @Override
    public void send(T message, ActorRef<T> to) throws RemoteException {
        reference.send(message, to);
    }

    @Override
    public String getName() throws RemoteException {
        return reference.getName();
    }

    @Override
    public void setOriginalSender(ActorRef<T> originalSender) throws RemoteException {
        reference.setOriginalSender(originalSender);
    }

    @Override
    public ActorRef<T> getOriginalSender() throws RemoteException {
        return reference.getOriginalSender();
    }

    @Override
    public boolean equals(Object obj) {
        return reference.equals(obj);
    }

    @Override
    public int hashCode() {
        return reference.hashCode();
    }
}
