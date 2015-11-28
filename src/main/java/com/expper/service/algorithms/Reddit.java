package com.expper.service.algorithms;

import com.expper.domain.Post;
import com.qiniu.common.Zone;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class Reddit {
    public static final long BASE_TIME = ZonedDateTime.parse("2015-01-01T00:00:00.0+08:00[Asia/Shanghai]").toEpochSecond();

    public void updateHots(List<PostMeta> posts) {
        posts.forEach(post -> {
            post.hot = (int) (hot(post.ups, post.downs, post.createdAt) * 1000000);
        });
    }

    public void sort(List<PostMeta> posts) {
        posts.sort(new HotComparator());
    }

    private static long age(ZonedDateTime date) {
        return date.toEpochSecond() - BASE_TIME;
    }

    private static int score(int ups, int downs) {
        return ups - downs;
    }

    public static double hot(int ups, int downs, ZonedDateTime date) {
        int score = score(ups, downs);
        // double order = Math.log10(Math.max(Math.abs(score), 1));
        double order = Math.log(Math.max(Math.abs(score), 1));  // 换成自然对数，网站开始时点赞数太少
        int sign = score > 0 ? 1 : (score < 0 ? -1 : 0);
        return order + (double) (sign * age(date)) / 45000.0;
    }

    public static double hot(Post post) {
        double hot = hot(post.getVotesUp(), post.getVotesDown(), post.getShareAt());

        return Math.round(hot * 1000000.0) / 1000000.0;
    }

    public static double hot(Post post, int ups, int downs, int replies, int hits) {
        ups += hits / 100 + replies / 2; // TODO
        double hot = hot(ups, downs, post.getShareAt());

        return Math.round(hot * 1000000.0) / 1000000.0;
    }
}
