package com.expper.web.rest.dto;

import com.expper.domain.Post;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class PostVoteDTO {
    private Long id;

    @JsonProperty("votes_up")
    private int votesUp;

    @JsonProperty("votes_down")
    private int votesDown;

    private String result;

    public PostVoteDTO() {
    }

    public PostVoteDTO(Post post,String result) {
        this.votesUp = post.getVotesUp();
        this.votesDown = post.getVotesDown();
        this.id = post.getId();
        this.result = result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
