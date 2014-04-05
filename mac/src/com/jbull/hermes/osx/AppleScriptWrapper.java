package com.jbull.hermes.osx;

import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class AppleScriptWrapper {
    public static String executeScript(String script) throws IOException, InterruptedException, ScriptException {
        String[] cmd = { "osascript", "-e", script };
        final Process p = Runtime.getRuntime().exec(cmd);
        int exitVal = p.waitFor();
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        String s = "";
        String t;
        while ((t = stdInput.readLine()) != null) {
            s+=t;
        }

        String e = "";
        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));

        while ((t = stdError.readLine()) != null) {
            e+=t;
        }
        //int exitVal = p.waitFor();
        System.err.println(e);
        System.out.println(exitVal);
        System.out.println(s);
        return s;
    }
}
