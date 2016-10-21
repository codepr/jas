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

import io.github.codepr.jas.actors.utils.ActorSystemFactory;
import io.github.codepr.jas.actors.utils.actors.genetic.*;
import io.github.codepr.jas.actors.utils.messages.genetic.*;
import io.github.codepr.jas.actors.utils.actors.TrivialActor;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * GeneticActor test class
 * @author Andrea Giacomo Baldan
 */
public class GeneticActorTest {

    private ActorSystem system;

    /**
     * Initializes the {@code system} with a concrete implementation before each test.
     */
    @Before
    public void init() {
        this.system = ActorSystemFactory.buildActorSystem();
    }

    @Test
    public void shouldCalcFittestIndividual() throws InterruptedException, RemoteException {
        TestActorRef popSampleRef = new TestActorRef(system.actorOf(GeneticActor.class));
        byte[] solution = new byte[32];
        // init the solution
        for (int i = 0; i < solution.length; i++) {
            solution[i] = 1;
        }
        solution[4] = 0;
        solution[18] = 0;
        solution[23] = 0;

        GeneticActor popSampleActor = (GeneticActor) popSampleRef.getUnderlyingActor(system);
        popSampleActor.initPopulationAndSolution(15, solution);
        while (((GeneticActor) popSampleRef.getUnderlyingActor(system)).getFitness() < 32) {
            TestActorRef nature = new TestActorRef(system.actorOf(TrivialActor.class));
            nature.send(new Evolve(), popSampleRef);
        }

        Thread.sleep(2000);

        Assert.assertEquals("The solution should be 11110111111111111101111011111111",
                "11110111111111111101111011111111",
                ((GeneticActor) popSampleRef.getUnderlyingActor(system)).printFittest());
    }

    /**
     * Stops the {@code system}
     */
    @After
    public void tearDown() throws RemoteException { system.stop(); }
}
