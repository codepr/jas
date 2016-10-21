package it.unipd.math.pcd.actors.remote;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteTask extends Remote {
    <T extends Serializable> T executeTask(Task<T> task) throws RemoteException;
}
