/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 Andrea Giacomo Baldan
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
 * Please, insert description here.
 *
 * @author Andrea Giacomo Baldan
 * @version 1.0
 * @since 1.0
 */

/**
 * Please, insert description here.
 *
 * @author Andrea Giacomo Baldan
 * @version 1.0
 * @since 1.0
 */
package io.github.codepr.jas.actors;

import io.github.codepr.jas.actors.exceptions.UnsupportedMessageException;
import io.github.codepr.jas.actors.utils.ActorSystemFactory;
import io.github.codepr.jas.actors.utils.actors.bouncer.BouncerActor;
import io.github.codepr.jas.actors.utils.messages.TrivialMessage;
import io.github.codepr.jas.actors.utils.messages.bouncer.BounceMessage;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Variant of the PingPong test
 * @author Andrea Giacomo Baldan
 * @version 1.0
 * @since 1.0
 */
public class BouncerActorTest {

    private ActorSystem system;

    /**
     * Initializes the {@code system} with a concrete implementation before each test.
     */
    @Before
    public void init() {
        this.system = ActorSystemFactory.buildActorSystem();
    }

    @Test
    public void shouldAnswerToStatements() throws InterruptedException, RemoteException {
        TestActorRef oracle = new TestActorRef(system.actorOf(BouncerActor.class));
        TestActorRef declarator = new TestActorRef(system.actorOf(BouncerActor.class));

        declarator.send(new BounceMessage("hi"), oracle);
        Thread.sleep(2000);
        Assert.assertEquals("Should answer hello", "Hello", ((BouncerActor) declarator.getUnderlyingActor(system)).getLastStatement());
        Assert.assertEquals("Should answer hello", "hi", ((BouncerActor) oracle.getUnderlyingActor(system)).getLastStatement());
    }

    @Test
    public void shouldAnswerToLastStatementAfterStop() throws InterruptedException, RemoteException {
        TestActorRef oracle = new TestActorRef(system.actorOf(BouncerActor.class));
        TestActorRef declarator = new TestActorRef(system.actorOf(BouncerActor.class));

        declarator.send(new BounceMessage("How are you?"), oracle);
        system.stop(oracle);
        Thread.sleep(2000);
        Assert.assertEquals("Should answer 'Fine.', even after being stopped", "Fine.", ((BouncerActor) declarator.getUnderlyingActor(system)).getLastStatement());
    }

    @Test(expected = UnsupportedMessageException.class)
    public void shouldGetUnsupportedMessageExceptionWithUnknownMessageType() {
        BouncerActor b = (BouncerActor) (new TestActorRef(system.actorOf(BouncerActor.class))).getUnderlyingActor(system);
        b.receive(new TrivialMessage());
    }

    /**
     * Stops the {@code system}
     */
    @After
    public void tearDown() throws RemoteException { system.stop(); }
}
