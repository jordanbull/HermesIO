package com.jbull.hermes.osx;

import com.jbull.hermes.desktop.Notification;

import javax.script.ScriptException;
import java.io.IOException;


public class MacNotification implements Notification {
    @Override
    public boolean notify(String sender, String content, byte[] imageData) {
        try {
            AppleScriptWrapper.executeScript("display notification \"" + content + "\" with title \"" + sender + "\"");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return false;
    }
}
