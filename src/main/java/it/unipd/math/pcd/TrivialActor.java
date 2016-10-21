package it.unipd.math.pcd;

import java.io.Serializable;

import it.unipd.math.pcd.actors.AbsActor;
import it.unipd.math.pcd.actors.Message;

public class TrivialActor extends AbsActor<TrivialMessage> implements Serializable {
    private static final long serialVersionUID = 227L;

    @Override
    public void receive(TrivialMessage message) {
        // Do nothing.
        System.out.println("hello");
    }
}
