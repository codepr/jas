package io.github.codepr.jas.actors.remote;

import java.io.Serializable;

public interface Task<T extends Serializable> extends Serializable {
    T execute();
}
