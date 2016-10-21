package it.unipd.math.pcd.actors.remote;

import java.io.Serializable;

public interface Task<T extends Serializable> extends Serializable {
    T execute();
}
