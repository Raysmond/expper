package com.expper.domain;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.expper.domain.enumeration.PostStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.ZonedDateTime;

/**
 * A Post.
 */
@Entity
@Table(name = "post")
@Document(indexName = "post", type = "post")
public class Post extends AbstractModel implements Serializable {

    public static final int MAX_SUMMARY_SIZE = 300;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "url")
    private String url;

    @Column(name = "domain")
    @NotNull
    private String domain;

    @Column(name = "author")
    private String author;

    @Column(name = "summary")
    private String summary;

    @Size(max = 1048576)
    @Column(name = "content", length = 1048576)
    private String content;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostStatus status = PostStatus.PRIVATE;

    @ManyToOne
    private User user;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt = null;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt = null;

    @Column(name = "share_at")
    private ZonedDateTime shareAt;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "posts_tags",
        joinColumns = {@JoinColumn(name = "post_id", nullable = false, updatable = false)},
        inverseJoinColumns = {@JoinColumn(name = "tag_id", nullable = false, updatable = false)}
    )
    private Set<Tag> tags = new HashSet<>();

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "post")
    private Set<Vote> votes = new HashSet<>();

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "post")
    private Set<Reply> replyList = new HashSet<>();


    @JsonIgnore
    @OneToMany(orphanRemoval = true, mappedBy = "post")
    private Set<Message> messages = new HashSet<>();

    // Up votes count
    @Column(name = "votes_up", nullable = false)
    public int votesUp = 0;

    // Down votes count
    @Column(name = "votes_down", nullable = false)
    public int votesDown = 0;

    // Total hits count
    @Column(name = "hits", nullable = false)
    public int hits = 0;

    // Replies count
    @Column(name = "replies", nullable = false)
    public int replies;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = updatedAt = ZonedDateTime.now();
        }

        if (status == PostStatus.PUBLIC) {
            shareAt = ZonedDateTime.now();
        }

        initSummary();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = ZonedDateTime.now();

        // Set shareAt to current time when the post status is set to PUBLIC for the first time
        if (shareAt == null && status == PostStatus.PUBLIC) {
            shareAt = ZonedDateTime.now();
        }

        initSummary();
    }

    public void initSummary() {
        if (summary == null && content != null && !content.isEmpty()) {
            int size = Math.min(Post.MAX_SUMMARY_SIZE, content.length());
            setSummary(Jsoup.clean(content.substring(0, size), Whitelist.basic()));
        }
    }

    public Set<Reply> getReplyList() {
        return replyList;
    }

    public void setReplyList(Set<Reply> replyList) {
        this.replyList = replyList;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSummary() {
        // For old post, the summary may be null
        if (summary == null) {
            initSummary();
        }
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ZonedDateTime getShareAt() {
        if (shareAt == null && status == PostStatus.PUBLIC) {
            shareAt = createdAt;
        }
        return shareAt;
    }

    public void setShareAt(ZonedDateTime shareAt) {
        this.shareAt = shareAt;
    }

    public int getVotesUp() {
        return votesUp;
    }

    public void setVotesUp(int votesUp) {
        this.votesUp = votesUp;
    }

    public int getVotesDown() {
        return votesDown;
    }

    public void setVotesDown(int votesDown) {
        this.votesDown = votesDown;
    }

    public void voteUp() {
        this.votesUp += 1;
    }

    public void cancelVoteUp() {
        this.votesUp -= 1;
    }

    public void voteDown() {
        this.votesDown += 1;
    }

    public void cancelVoteDown() {
        this.votesDown -= 1;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getReplies() {
        return replies;
    }

    public void setReplies(int replies) {
        this.replies = replies;
    }

    @Override
    public String toString() {
        return "Post{" +
            "title='" + title + '\'' +
            ", url='" + url + '\'' +
            ", status=" + status +
            ", shareAt=" + shareAt +
            ", createdAt=" + createdAt +
            '}';
    }
}
