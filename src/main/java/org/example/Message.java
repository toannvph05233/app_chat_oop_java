package org.example;

import java.io.Serializable;

public class Message implements Serializable {
    private String fromUser;
    private String toUser;
    private String content;

    public Message(String fromUser, String toUser, String content) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.content = content;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public String getContent() {
        return content;
    }
}

