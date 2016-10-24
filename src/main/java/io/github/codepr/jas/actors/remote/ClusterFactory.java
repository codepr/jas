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
package io.github.codepr.jas.actors.remote;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.net.UnknownHostException;
import io.github.codepr.jas.actors.ActorSystem;

/**
 * Factory class, instantiate a cluster
 */
public class ClusterFactory {

    public static final Cluster joinCluster(ActorSystem system) {
        Cluster cluster = null;
        try {
            cluster = joinCluster(system, InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return cluster;
    }

    public static final Cluster joinCluster(ActorSystem system, String host) {
        Cluster cluster = null;
        try {
            final String seedHost = InetAddress.getLocalHost().getHostAddress();
            cluster = new ClusterImpl(system, host, seedHost);
        } catch (UnknownHostException | RemoteException e) {
            e.printStackTrace();
        }
        return cluster;
    }

    public static final Cluster joinCluster(ActorSystem system, String host, String seedHost) {
        Cluster cluster = null;
        try {
            cluster = new ClusterImpl(system, host, seedHost);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return cluster;
    }
}
