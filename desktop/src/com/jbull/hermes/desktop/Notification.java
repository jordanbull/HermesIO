package com.jbull.hermes.desktop;

/**
 * Created by Jordan on 4/4/14.
 */
public interface Notification {
    public boolean notify(String sender, String content, byte[] imageData);
}
