package com.expper.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.ZonedDateTime;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import com.expper.domain.enumeration.ContentStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * A Content.
 */
@Entity
@Table(name = "content")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Getter
@Setter
public class Content extends AbstractTimedModel implements Serializable {

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Size(max = 1048576)
    @Column(name = "content", length = 1048576, nullable = false)
    private String content;

    @Size(max = 65535)
    @Column(name = "summary", length = 65535)
    private String summary;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContentStatus status = ContentStatus.DRAFT;

    @Column(name = "permalink")
    private String permalink;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Content{" +
            "id=" + getId() +
            ", title='" + title + "'" +
            ", content='" + content + "'" +
            ", summary='" + summary + "'" +
            ", status='" + status + "'" +
            ", permalink='" + permalink + "'" +
            ", createdAt='" + this.getCreatedAt() + "'" +
            ", updatedAt='" + this.getUpdatedAt() + "'" +
            '}';
    }
}
