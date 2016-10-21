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
 * Please, insert description here.
 *
 * @author Riccardo Cardin
 * @author Andrea Giacomo Baldan
 * @version 1.0
 * @since 1.0
 */

/**
 * Please, insert description here.
 *
 * @author Riccardo Cardin
 * @author Andrea Giacomo Baldan
 * @version 1.0
 * @since 1.0
 */
package it.unipd.math.pcd.actors;

import it.unipd.math.pcd.actors.utils.ActorSystemFactory;
import it.unipd.math.pcd.actors.utils.actors.TrivialActor;
import it.unipd.math.pcd.actors.utils.actors.counter.CounterActor;
import it.unipd.math.pcd.actors.utils.actors.longtask.LongTaskActor;
import it.unipd.math.pcd.actors.utils.actors.ping.pong.PingPongActor;
import it.unipd.math.pcd.actors.utils.actors.StoreActor;
import it.unipd.math.pcd.actors.utils.messages.StoreMessage;
import it.unipd.math.pcd.actors.utils.messages.TrivialMessage;
import it.unipd.math.pcd.actors.utils.messages.counter.Decrement;
import it.unipd.math.pcd.actors.utils.messages.counter.Increment;
import it.unipd.math.pcd.actors.utils.messages.ping.pong.PingMessage;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * Integration test suite on actor features.
 *
 * @author Riccardo Cardin
 * @author Andrea Giacomo Baldan
 * @version 1.0
 * @since 1.0
 */
public class ActorIT {

    private ActorSystem system;

    /**
     * Initializes the {@code system} with a concrete implementation before each test.
     */
    @Before
    public void init() {
        this.system = ActorSystemFactory.buildActorSystem();
    }

    @Test
    public void shouldBeAbleToSendAMessage() throws InterruptedException, RemoteException {
        TestActorRef ref = new TestActorRef(system.actorOf(StoreActor.class));
        StoreActor actor = (StoreActor) ref.getUnderlyingActor(system);
        // Send a string to the actor
        ref.send(new StoreMessage("Hello World"), ref);
        // Wait that the message is processed
        Thread.sleep(2000);
        // Verify that the message is been processed
        Assert.assertEquals("The message has to be received by the actor", "Hello World", actor.getData());
    }

    @Test
    public void shouldBeAbleToRespondToAMessage() throws InterruptedException, RemoteException {
        TestActorRef pingRef = new TestActorRef(system.actorOf(PingPongActor.class));
        TestActorRef pongRef = new TestActorRef(system.actorOf(PingPongActor.class));

        pongRef.send(new PingMessage(), pingRef);

        Thread.sleep(2000);

        PingPongActor pingActor = (PingPongActor) pingRef.getUnderlyingActor(system);
        PingPongActor pongActor = (PingPongActor) pongRef.getUnderlyingActor(system);

        Assert.assertEquals("A ping actor has received a ping message", "Ping",
                            pingActor.getLastMessage().getMessage());
        Assert.assertEquals("A pong actor has received back a pong message", "Pong",
                            pongActor.getLastMessage().getMessage());
    }

    @Test
    public void shouldNotLooseAnyMessage() throws InterruptedException, RemoteException {
        TestActorRef counter = new TestActorRef(system.actorOf(CounterActor.class));
        for (int i = 0; i < 200; i++) {
            TestActorRef adder = new TestActorRef(system.actorOf(TrivialActor.class));
            adder.send(new Increment(), counter);
        }

        Thread.sleep(2000);

        Assert.assertEquals("A counter that was incremented 1000 times should be equal to 1000",
                            200, ((CounterActor) counter.getUnderlyingActor(system)).getCounter());
    }

    /************* ADDITIONAL TESTS *****************/

    @Test
    public void shouldSynchronizedIncrementOrDecrement() throws InterruptedException, RemoteException {
        final TestActorRef counter = new TestActorRef(system.actorOf(CounterActor.class));
        new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 5500; i++) {
                        TestActorRef adder = new TestActorRef(system.actorOf(TrivialActor.class));
                        try {
                            adder.send(new Increment(), counter);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 1500; i++) {
                        TestActorRef subtractor = new TestActorRef(system.actorOf(TrivialActor.class));
                        try {
                            subtractor.send(new Decrement(), counter);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        for (int i = 0; i < 2250; i++) {
            TestActorRef adder = new TestActorRef(system.actorOf(TrivialActor.class));
            try {
                adder.send(new Decrement(), counter);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        Thread.sleep(2000);
        Assert.assertEquals("Final counter value should be 1750",
                            1750, ((CounterActor) counter.getUnderlyingActor(system)).getCounter());
    }

    @Test
    public void shouldProcessRemainingMessagesAfterStop() throws RemoteException {
        TestActorRef ref1 = new TestActorRef(system.actorOf(LongTaskActor.class));
        TestActorRef ref2 = new TestActorRef(system.actorOf(TrivialActor.class));
        LongTaskActor lta = (LongTaskActor) ref1.getUnderlyingActor(system);

        for (int i = 0; i < 3; i++) {
            ref2.send(new TrivialMessage(), ref1);
        }
        system.stop(ref1);
        Assert.assertEquals("should read Done 3 times", "Done 3 times", lta.getTask());
    }

    @Test
    public void shouldProcessRemainingMessagesAfterSystemStopped() throws RemoteException {
        TestActorRef ref1 = new TestActorRef(system.actorOf(LongTaskActor.class));
        TestActorRef ref2 = new TestActorRef(system.actorOf(TrivialActor.class));
        LongTaskActor lta = (LongTaskActor) ref1.getUnderlyingActor(system);

        for (int i = 0; i < 3; i++) {
            ref2.send(new TrivialMessage(), ref1);
        }
        system.stop();
        Assert.assertEquals("should read Done 3 times", "Done 3 times", lta.getTask());
    }

    /**
     * Stops the {@code system}
     */
    @After
    public void tearDown() throws RemoteException { system.stop(); }
}
