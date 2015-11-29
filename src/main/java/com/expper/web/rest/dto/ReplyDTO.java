package com.expper.web.rest.dto;

import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;

import com.expper.domain.Post;
import com.expper.domain.Reply;
import com.expper.domain.User;
import com.expper.domain.enumeration.ReplyStatus;
import com.expper.web.utils.ViewUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * A DTO for the Reply entity.
 */
public class ReplyDTO implements Serializable {
    private Long id;

    @NotNull
    @Size(min = 3, max = 65535)
    private String content;

    private ReplyStatus status;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("reply_to_id")
    private Long replyToId;

    private String username;

    @JsonProperty("created_at_s")
    private String createdAtS;

    @JsonProperty("user_picture")
    private String userPicture;

    public ReplyDTO(){

    }

    public ReplyDTO(Reply reply){
        this.id = reply.getId();
        this.content = reply.getContent();
        this.createdAt = reply.getCreatedAt();
        this.userId = reply.getUser().getId();
        this.username = reply.getUser().getLogin();
        this.createdAtS = ViewUtils.timeAgoOf(reply.getCreatedAt());
        this.postId = reply.getPost().getId();
        this.userPicture = ViewUtils.pictureUrl(reply.getUser().getPicture());
    }

    public Reply toReply(){
        Reply reply = new Reply();
        reply.setId(this.getId());
        reply.setContent(this.getContent());

        User user = new User();
        user.setId(this.getUserId());
        reply.setUser(user);

        Post post = new Post();
        post.setId(this.getPostId());
        reply.setPost(post);

        if (this.replyToId != null){
            Reply parent = new Reply();
            parent.setId(this.getReplyToId());
            reply.setReplyTo(parent);
        }

        return reply;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ReplyStatus getStatus() {
        return status;
    }

    public void setStatus(ReplyStatus status) {
        this.status = status;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
    public Long getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(Long replyId) {
        this.replyToId = replyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCreatedAtS() {
        return createdAtS;
    }

    public void setCreatedAtS(String createdAtS) {
        this.createdAtS = createdAtS;
    }


    public String getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(String userPicture) {
        this.userPicture = userPicture;
    }
}
