package com.expper.domain;

import com.expper.service.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.time.ZonedDateTime;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Entity
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tags", indexes = {
    @Index(name = "tags_idx_name", columnList = "name", unique = true)
})
public class Tag extends AbstractModel {
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull
    @Column(name = "friendly_name", nullable = false)
    @JsonProperty("friendly_name")
    private String friendlyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user; // tag creator

    @Column(name = "created_at", nullable = false)
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @Column(name = "post_count", nullable = false)
    private Long postCount = 0L;

    @Column(name = "followers_count", nullable = false)
    @JsonProperty("followers_count")
    private Long followersCount = 0L;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    private Set<Post> posts = new HashSet<>();

    public Tag() {

    }

    public Tag(User user, String name, String friendlyName) {
        this.user = user;
        this.name = name;
        this.friendlyName = friendlyName;
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = ZonedDateTime.now();
    }

    public void increasePostCountByOne() {
        postCount += 1;
    }

    public void decreasePostCountByOne() {
        if (postCount > 0){
            postCount -= 1;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
        this.name = StringUtil.toMachineString(friendlyName);
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getPostCount() {
        return postCount;
    }

    public void setPostCount(Long postCount) {
        this.postCount = postCount;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public Set<Post> getPosts() {
        return posts;
    }

    @Override
    public String toString() {
        return "Tag{" +
            "friendlyName='" + friendlyName + '\'' +
            ", name='" + name + '\'' +
            ", createdAt=" + createdAt +
            ", postCount=" + postCount +
            ", followersCount=" + followersCount +
            '}';
    }
}
