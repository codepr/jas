package it.unipd.math.pcd.actors.remote;

import java.io.Serializable;
import java.rmi.RemoteException;

public class RemoteTaskExecutor implements RemoteTask {
    public <T extends Serializable> T executeTask(Task<T> task) throws RemoteException {
        return task.execute();
    }
}
