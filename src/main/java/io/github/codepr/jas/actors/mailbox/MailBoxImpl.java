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
package io.github.codepr.jas.actors.mailbox;

import io.github.codepr.jas.actors.Message;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A mailbox system in the <code>pcd-actors</code> system to store incoming
 * messages.
 *
 * @author Andrea Giacomo Baldan
 * @version 1.0
 * @since 1.0
 */
public class MailBoxImpl<T extends Message> implements MailBox<T> {

    /**
     * Blocking queue for messages
     */
    private BlockingQueue<T> box;

    public MailBoxImpl() {
        box = new LinkedBlockingQueue<>();
    }

    /**
     * Enqueue incoming messages inside the structure of choice
     *
     * @param message The message to be stored
     */
    public void enqueue(T message) {
        try {
            box.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the head message of the queue
     *
     * @return The last message stored inside the queue
     */
    public T remove() {
        T message = null;
        try {
            message = box.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Check if the queue is empty
     *
     * @return True if the queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return box.isEmpty();
    }
}
