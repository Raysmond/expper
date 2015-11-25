package com.expper.service;

import com.expper.domain.Post;
import com.expper.domain.Tag;
import com.expper.domain.Topic;
import com.expper.domain.enumeration.PostStatus;
import com.expper.repository.PostRepository;
import com.expper.repository.TagRepository;
import com.expper.service.algorithms.Reddit;
import com.expper.web.exceptions.PageNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class HotPostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TopicsService topicsService;

    @Autowired
    private CountingService countingService;

    @Resource(name = "redisListTemplate")
    ZSetOperations<String, Long> hotPosts;

    public static final String CACHE_HOT_POSTS = "hot_posts";
    public static final String CACHE_HOT_TAG_POSTS = "hot_posts_tag_";
    public static final String CACHE_HOT_TOPIC_POSTS = "hot_posts_topic_";

    public static final int MAX_HOT_POSTS_SIZE = 2000; // 20*100, 100 pages


    public double redditScore(Post post) {
        return Reddit.hot(post,
            countingService.getVoteUp(post.getId()),
            countingService.getVoteDown(post.getId()),
            countingService.getReplies(post.getId()),
            countingService.getPostHits(post.getId()));
    }

    public void removeHotPost(Post post) {
        hotPosts.remove(CACHE_HOT_POSTS, post.getId());
    }

    public void removeTaggedPost(Post post, Set<Tag> tags) {
        List<Topic> topics = topicsService.getAll();

        tags.forEach(tag -> {
            // Remove from hot post list of tag
            hotPosts.remove(CACHE_HOT_TAG_POSTS + tag.getId(), post.getId());

            // Remove from hot post list of topic
            topics.forEach(topic -> {
                if (topic.getTags().contains(tag)) {
                    hotPosts.remove(CACHE_HOT_TOPIC_POSTS + topic.getId(), post.getId());
                }
            });
        });
    }

    public void addHotPost(Post post) {
        double score = redditScore(post);
        hotPosts.add(CACHE_HOT_POSTS, post.getId(), score);
        long size = hotPosts.size(CACHE_HOT_POSTS);
        if (size > MAX_HOT_POSTS_SIZE) {
            hotPosts.removeRange(CACHE_HOT_POSTS, 0, size - MAX_HOT_POSTS_SIZE);
        }
    }

    public void addTaggedPost(Post post, Set<Tag> tags) {
        List<Topic> topics = topicsService.getAll();
        double score = redditScore(post);

        tags.forEach(tag -> {
            // Add to hot post list of tag
            hotPosts.add(CACHE_HOT_TAG_POSTS + tag.getId(), post.getId(), score);

            // Add to hot post list of topic
            topics.forEach(topic -> {
                if (topic.getTags().contains(tag)) {
                    hotPosts.add(CACHE_HOT_TOPIC_POSTS + topic.getId(), post.getId(), score);
                }
            });
        });
    }

    public Set<ZSetOperations.TypedTuple<Long>> getPageWithScore(int page, int pageSize) {
        return hotPosts.reverseRangeWithScores(CACHE_HOT_POSTS, page * pageSize, (page + 1) * pageSize - 1);
    }

    public Set<ZSetOperations.TypedTuple<Long>> getPageWithScoreOfTag(Long tagId, int page, int pageSize) {
        return hotPosts.reverseRangeWithScores(CACHE_HOT_TAG_POSTS + tagId, page * pageSize, (page + 1) * pageSize - 1);
    }

    public Set<ZSetOperations.TypedTuple<Long>> getPageWithScoreOfTopic(Long topicId, int page, int pageSize) {
        return hotPosts.reverseRangeWithScores(CACHE_HOT_TOPIC_POSTS + topicId, page * pageSize, (page + 1) * pageSize - 1);
    }

    public long size() {
        return hotPosts.size(CACHE_HOT_POSTS);
    }

    public long sizeOfTag(Tag tag) {
        return hotPosts.size(CACHE_HOT_TAG_POSTS + tag.getId());
    }

    public long sizeOfTopic(Long topicId) {
        return hotPosts.size(CACHE_HOT_TOPIC_POSTS + topicId);
    }

    public void sortByScore(List<Post> posts, Set<ZSetOperations.TypedTuple<Long>> scores) {
        Map<Long, Double> map = new HashMap<>();
        scores.forEach(s -> map.put(s.getValue(), s.getScore()));

        posts.sort((o1, o2) -> {
            double diff = map.get(o2.getId()) - map.get(o1.getId());
            return diff > 0 ? 1 : (diff < 0 ? -1 : 0);
        });
    }

    @PostConstruct
    public void init() {
        if (size() == 0) {
            this.initHotPosts();
        }
    }

    // TODO
    @Transactional(readOnly = true)
    public void initHotPosts() {
        PageRequest page = new PageRequest(0, Integer.MAX_VALUE, Sort.Direction.DESC, "id");
        Page<Post> posts = postRepository.findPublicPosts(page, PostStatus.PUBLIC);
        posts.forEach(post -> {
            this.addHotPost(post);
            this.addTaggedPost(post, tagRepository.findPostTags(post.getId()));
        });
    }
}
