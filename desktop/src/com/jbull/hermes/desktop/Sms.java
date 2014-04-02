package com.jbull.hermes.desktop;

import java.io.Serializable;

public class Sms implements Serializable {
    private String content;
    private Long timeMillis;
    private boolean senderOfMessage;

    public Sms(String content, Long timeMillis, boolean senderOfMessage) {
        this.content = content;
        this.timeMillis = timeMillis;
        this.senderOfMessage = senderOfMessage;
    }

    public String getContent() {
        return content;
    }

    public Long getTimeMillis() {
        return timeMillis;
    }
    public boolean isSenderOfMessage() {
        return senderOfMessage;
    }

    public boolean equals(Object obj) {
        Sms msg2 = (Sms) obj;
        return msg2.timeMillis.equals(timeMillis) && msg2.content.equals(content) && senderOfMessage==msg2.senderOfMessage;
    }
}