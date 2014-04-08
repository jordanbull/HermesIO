package com.jbull.hermes;

import java.io.IOException;

public interface Sender<T> {
    public void send(T msg) throws IOException;
}
