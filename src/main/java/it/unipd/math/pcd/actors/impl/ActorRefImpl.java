package it.unipd.math.pcd.actors.impl;

import java.rmi.RemoteException;
import it.unipd.math.pcd.actors.ActorSystem;
import it.unipd.math.pcd.actors.ActorSystem.ActorMode;
import it.unipd.math.pcd.actors.Message;

public class ActorRefImpl<T extends Message> extends AbsActorRef<T> {

    public ActorRefImpl(ActorSystem system, ActorMode mode, String name) throws RemoteException {
        super(system, mode, name);
    }
}
