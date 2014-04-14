package com.jbull.hermes;


public abstract class Logger {
    private static Logger logger = new PrintLogger();

    public static void setLogger(Logger logger) {
        Logger.logger = logger;
    }

    public static void log(String message) {
        logger.doLog(message);
    }

    public static void log(Exception e) {
        logger.doLog(e);
    }

    protected abstract void doLog(String message);
    protected abstract void doLog(Exception e);
}
