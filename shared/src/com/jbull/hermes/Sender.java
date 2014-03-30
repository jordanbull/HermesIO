package com.jbull.hermes;

public interface Sender<T> {
    public void send(T msg);
}
