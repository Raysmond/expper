package com.expper.service.algorithms;

import org.joda.time.DateTime;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class HackNews {
    private final double gravity;
    private final long now;

    public HackNews(ZonedDateTime now, double gravity) {
        this.now = now.toEpochSecond();
        this.gravity = gravity;
    }

    public double score(PostMeta post) {
        return ((double) post.ups - 1.0) / Math.pow((double) ((now - post.createdAt.toEpochSecond()) / 3600 + 2), gravity);
    }

    public void updateHots(List<PostMeta> posts) {
        posts.forEach(post -> post.hot = (int) Math.round(score(post) * 1000000));
    }

    public void sort(List<PostMeta> posts) {
        posts.sort(new HotComparator());
    }
}
