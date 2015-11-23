package com.expper.service.support;

import com.expper.domain.Post;
import com.expper.domain.enumeration.PostStatus;

import java.util.Date;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class PostMeta {
    private Long id;
    private String title;
    private String domain;
    private String url;
    private Date createdAt;
    private PostStatus status;

    public PostMeta() {

    }

    public PostMeta(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.domain = post.getDomain();
        this.url = post.getUrl();
        this.status = post.getStatus();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }
}
