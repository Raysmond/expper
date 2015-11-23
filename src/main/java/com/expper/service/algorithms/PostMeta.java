package com.expper.service.algorithms;

import org.joda.time.DateTime;

import java.time.ZonedDateTime;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class PostMeta implements Comparable<PostMeta> {
    public long id;
    public int hot = 0;
    public int ups = 0;
    public int downs = 0;
    public int hits = 0;
    public int replies = 0;
    public ZonedDateTime createdAt;

    @Override
    public int compareTo(PostMeta o) {
        return id > o.id ? 1 : (id < o.id ? -1 : 0);
    }

    @Override
    public String toString() {
        return String.format(
            "PostMeta: {id=%s, hot=%s, createdAt=%s, ups=%s, downs=%s, hits=%s, replies=%s}",
            id, hot, createdAt, ups, downs, hits, replies);
    }
}
