package com.mmq.chatlite;

import com.google.firebase.database.Exclude;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Jon on 7/27/2017.
 */

public class Messages {

    @Exclude
    private boolean isMe;

    private String uid;
    private String key;
    private String sender;
    private String message;
    private String avatar;
    private Object timestamp;
    private boolean isImage;

    public Messages() {
    }

    public Messages(String uid, String key, String sender, String message, String avatar, Object timestamp, boolean isImage) {
        this.uid = uid;
        this.key = key;
        this.sender = sender;
        this.message = message;
        this.avatar = avatar;
        this.timestamp = timestamp;
        this.isImage = isImage;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public String getSentTime() {
        try {
        Long castedTimestamp = Long.parseLong(timestamp.toString());
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(castedTimestamp));
        } catch (Exception ex) { return "00:00"; }
    }

    public void setTimestamp(Object time) {
        this.timestamp = time;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
