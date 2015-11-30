package com.expper.domain;

import com.expper.domain.enumeration.MessageStatus;
import com.expper.domain.enumeration.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.annotations.Type;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.ZonedDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * @author Raysmond<i@raysmond.com>
 */
@Entity
@Table(name = "message")
public class Message extends AbstractModel {

    private String title;

    private String content;

    @JsonProperty("to_user")
    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User toUser;

    @JsonProperty("by_user")
    @ManyToOne
    @JoinColumn(name = "by_user_id")
    private User byUser;

    @ManyToOne
    private Post post;

    @JsonProperty("type")
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType type = MessageType.SYSTEM_MESSAGE;

    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.UNREAD;

    @Column(name = "created_at", nullable = false)
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] params;

    public Message() {
    }

    public Message(User toUser, User byUser, String title, String content, Post post, MessageType type) {
        this.title = title;
        this.content = content;
        this.toUser = toUser;
        this.byUser = byUser;
        this.post = post;
        this.type = type;
    }

    @PrePersist
    private void prePersist() {
        createdAt = ZonedDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public User getByUser() {
        return byUser;
    }

    public void setByUser(User byUser) {
        this.byUser = byUser;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public byte[] getParams() {
        return params;
    }

    @Transient
    public JSONObject getParamsJson() throws JSONException {
        if (params == null)
            return null;
        return new JSONObject(new String(params));
    }

    public void setParams(JSONObject params) {
        this.params = params.toString().getBytes();
    }

    public void addParam(String key, Object value) {
        try {
            JSONObject json = this.getParamsJson();
            if (json == null) {
                json = new JSONObject();
            }
            json.put(key, value);
            this.setParams(json);
        } catch (JSONException ignored) {
        }
    }

    @Override
    public String toString() {
        return "Message{" +
            "title='" + title + '\'' +
            ", content='" + content + '\'' +
            ", toUser=" + toUser +
            ", byUser=" + byUser +
            ", post=" + post +
            ", type=" + type +
            ", status=" + status +
            ", createdAt=" + createdAt +
            '}';
    }
}
