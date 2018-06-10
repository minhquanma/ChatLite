package com.mmq.chatlite;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mmq on 05/10/2017.
 */

public class Rooms {
    String id;
    String avatar;
    String name;
    String lastMessage;
    Long timestamp;
    boolean isRead;

    public Rooms() {
    }

    public Rooms(String id, String avatar, String name) {
        this.id = id;
        this.avatar = avatar;
        this.name = name;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSentTime() {
        try {
            return DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(timestamp));
        } catch (Exception ex) { return "00:00"; }
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
