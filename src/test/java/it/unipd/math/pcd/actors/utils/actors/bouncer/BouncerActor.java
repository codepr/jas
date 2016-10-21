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
package it.unipd.math.pcd.actors.utils.actors.bouncer;

import java.rmi.RemoteException;

import it.unipd.math.pcd.actors.AbsActor;
import it.unipd.math.pcd.actors.exceptions.UnsupportedMessageException;
import it.unipd.math.pcd.actors.Message;
import it.unipd.math.pcd.actors.utils.messages.bouncer.BounceMessage;
import it.unipd.math.pcd.actors.utils.messages.bouncer.ResponseMessage;

/**
 * BounceActor, variant class of the {@code PingPongActor}
 * @author Andrea Giacomo Baldan
 * @version 1.0
 * @since 1.0
 */
public class BouncerActor extends AbsActor<Message> {

    String lastStatement;

    public String getLastStatement() {
        return lastStatement;
    }

    @Override
    public void receive(Message message) {
        if (message instanceof BounceMessage) {
            lastStatement = ((BounceMessage) message).getStatement();
            switch (((BounceMessage) message).getStatement().toLowerCase()) {
            case "hi":
                try {
                    self.send(new ResponseMessage("Hello"), sender);
                } catch(RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case "how are you?":
                try {
                    self.send(new ResponseMessage("Fine."), sender);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                try {
                    self.send(new ResponseMessage("42"), sender);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
        } else if (message instanceof ResponseMessage) {
            lastStatement = ((ResponseMessage) message).getResponse();
        } else {
            throw new UnsupportedMessageException(message);
        }
    }
}
