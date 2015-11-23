package com.expper.web.rest.dto;

import com.expper.domain.Tag;

import java.io.Serializable;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class PostTagCountDTO implements Serializable{
    private Long count = 0L;
    private Tag tag;

    public PostTagCountDTO(Long count, Tag tag){
        this.tag = tag;
        this.count = count;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
