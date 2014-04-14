package com.jbull.hermes;

public class PrintLogger extends Logger {

    @Override
    protected void doLog(String message) {
        System.out.println(message);
    }

    @Override
    protected void doLog(Exception e) {
        e.printStackTrace();
    }
}
