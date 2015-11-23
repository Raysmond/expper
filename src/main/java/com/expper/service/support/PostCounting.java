package com.expper.service.support;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class PostCounting {
    private Long id;
    private Integer hits;
    private Integer votesUp;
    private Integer votesDown;
    private Integer replies;

    public PostCounting() {
    }

    public PostCounting(Long id, Integer hits, Integer votesUp, Integer votesDown, Integer replies) {
        this.id = id;
        this.hits = hits;
        this.votesUp = votesUp;
        this.votesDown = votesDown;
        this.replies = replies;
    }

    public int getVotes() {
        return votesUp - votesDown;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getHits() {
        return hits;
    }

    public void setHits(Integer hits) {
        this.hits = hits;
    }

    public Integer getVotesUp() {
        return votesUp;
    }

    public void setVotesUp(Integer votesUp) {
        this.votesUp = votesUp;
    }

    public Integer getVotesDown() {
        return votesDown;
    }

    public void setVotesDown(Integer votesDown) {
        this.votesDown = votesDown;
    }

    public Integer getReplies() {
        return replies;
    }

    public void setReplies(Integer replies) {
        this.replies = replies;
    }

    @Override
    public String toString() {
        return "PostCounting{" +
            "id=" + id +
            ", hits=" + hits +
            ", votesUp=" + votesUp +
            ", votesDown=" + votesDown +
            ", replies=" + replies +
            '}';
    }
}
