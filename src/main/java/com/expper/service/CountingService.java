package com.expper.service;

import com.expper.config.RabbitmqConfiguration;
import com.expper.domain.Post;
import com.expper.repository.PostRepository;
import com.expper.repository.TagRepository;
import com.expper.repository.UserRepository;
import com.expper.service.support.PostCounting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class CountingService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Resource(name = "redisCountingTemplate")
    HashOperations<String, String, Integer> counting;

    @Resource(name = "redisListTemplate")
    SetOperations<String, Long> changeList;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final Logger log = LoggerFactory.getLogger(CountingService.class);

    public static final String CACHE_CHANGE_LIST = "counting.changes";
    public static final String CACHE_COUNTING = "counting";
    public static final String CACHE_VOTE = "counting.vote";
    public static final String CACHE_REPLIES = "counting.replies";
    public static final String CACHE_PAGE_VIEWS = "counting.pv";
    public static final String CACHE_USER_POSTS = "counting.user.posts";


    public Integer getUsersCount() {
        Integer val = counting.get(CACHE_COUNTING, "users");
        if (val == null) {
            val = (int) userRepository.count();
            counting.put(CACHE_COUNTING, "users", val);
        }

        return val;
    }

    public Integer getPublicPostsCount() {
        Integer val = counting.get(CACHE_COUNTING, "public_posts");
        if (val == null) {
            val = (int) postRepository.countPublicPost();
            counting.put(CACHE_COUNTING, "public_posts", val);
        }

        return val;
    }

    public Integer getTagsCount() {
        Integer val = counting.get(CACHE_COUNTING, "tags");
        if (val == null) {
            val = (int) tagRepository.count();
            counting.put(CACHE_COUNTING, "tags", val);
        }

        return val;
    }

    public void incUsersCount() {
        counting.increment(CACHE_COUNTING, "users", 1);
    }

    public void incPublicPostsCount() {
        counting.increment(CACHE_COUNTING, "public_posts", 1);
    }

    public void incTagsCount() {
        counting.increment(CACHE_COUNTING, "tags", 1);
    }

    public void decUsersCount() {
        counting.increment(CACHE_COUNTING, "users", -1);
    }

    public void decPublicPostsCount() {
        counting.increment(CACHE_COUNTING, "public_posts", -1);
    }

    public void decTagsCount() {
        counting.increment(CACHE_COUNTING, "tags", -1);
    }

    public void loadPostCounting(Long postId) {
        Post post = postRepository.findOne(postId);

        counting.put(CACHE_VOTE, "votes_up_" + postId, post.getVotesUp());
        counting.put(CACHE_VOTE, "votes_down_" + postId, post.getVotesDown());
        counting.put(CACHE_REPLIES, postId.toString(), post.getReplies());
        counting.put(CACHE_PAGE_VIEWS, "/posts/" + postId, post.getHits());
    }

    public Map<Long, PostCounting> getPostListCounting(Collection<Long> postIds) {
        Map<Long, PostCounting> counting = new HashMap<>();
        postIds.forEach(id -> counting.put(id, getPostCounting(id)));
        return counting;
    }

    public Map<Long, PostCounting> getPostListCounting(List<Post> posts) {
        Map<Long, PostCounting> counting = new HashMap<>();
        posts.forEach(post -> counting.put(post.getId(), getPostCounting(post.getId())));
        return counting;
    }

    public PostCounting getPostCounting(Long postId) {
        PostCounting postCounting = new PostCounting();

        postCounting.setId(postId);
        postCounting.setVotesUp(getVoteUp(postId));
        postCounting.setVotesDown(getVoteDown(postId));
        postCounting.setReplies(getReplies(postId));
        postCounting.setHits(getPostHits(postId));

        return postCounting;
    }

    public Integer getVoteUp(Long postId) {
        Integer val = counting.get(CACHE_VOTE, "votes_up_" + postId);
        if (val == null) {
            loadPostCounting(postId);
        }
        return counting.get(CACHE_VOTE, "votes_up_" + postId);
    }

    public Integer getVoteDown(Long postId) {
        Integer val = counting.get(CACHE_VOTE, "votes_down_" + postId);
        if (val == null) {
            loadPostCounting(postId);
        }
        return counting.get(CACHE_VOTE, "votes_down_" + postId);
    }

    public Integer getReplies(Long postId) {
        Integer val = counting.get(CACHE_REPLIES, postId.toString());
        if (val == null) {
            loadPostCounting(postId);
        }
        return counting.get(CACHE_REPLIES, postId.toString());
    }

    public Integer getPageView(String url) {
        return counting.get(CACHE_PAGE_VIEWS, url);
    }

    public void incVoteUp(Long postId) {
        counting.increment(CACHE_VOTE, "votes_up_" + postId, 1);
        changeList.add(CACHE_CHANGE_LIST, postId);
    }

    public void incVoteDown(Long postId) {
        counting.increment(CACHE_VOTE, "votes_down_" + postId, 1);
        changeList.add(CACHE_CHANGE_LIST, postId);
    }

    public void decVoteUp(Long postId) {
        counting.increment(CACHE_VOTE, "votes_up_" + postId, -1);
        changeList.add(CACHE_CHANGE_LIST, postId);
    }

    public void decVoteDown(Long postId) {
        counting.increment(CACHE_VOTE, "votes_down_" + postId, -1);
        changeList.add(CACHE_CHANGE_LIST, postId);
    }

    public void incReplies(Long postId) {
        counting.increment(CACHE_REPLIES, postId.toString(), 1);
        changeList.add(CACHE_CHANGE_LIST, postId);
    }

    public void decReplies(Long postId) {
        counting.increment(CACHE_REPLIES, postId.toString(), -1);
        changeList.add(CACHE_CHANGE_LIST, postId);
    }

    public void incPageViews(String url) {
        counting.increment(CACHE_PAGE_VIEWS, url, 1);
    }

    public void incPostHits(Long postId) {
        incPageViews("/posts/" + postId);
        changeList.add(CACHE_CHANGE_LIST, postId);

        if (getPostHits(postId) % 100 == 0) {
            rabbitTemplate.convertAndSend(RabbitmqConfiguration.QUEUE_UPDATE_POST_SCORE, postService.getPost(postId));
        }
    }

    public Integer getPostHits(Long postId) {
        Integer val = getPageView("/posts/" + postId);
        if (val == null) {
            loadPostCounting(postId);
        }

        return getPageView("/posts/" + postId);
    }

    /**
     * Use Spring scheduling framework to save counting information Schedule time: every 10 seconds
     */
    @Scheduled(fixedRate = 10000)
    public void saveCountingsInBatch() {
        if (changeList.size(CACHE_CHANGE_LIST) > 0) {
            // TODO save max 10 changes each time ?
            saveCountings(10);
        }
    }

    private void saveCountings(int batchSize) {
        log.debug("Scheduled to save counting information, size=" + batchSize);

        List<Long> ids = new ArrayList<>();

        batchSize = (int) Math.min(batchSize, changeList.size(CACHE_CHANGE_LIST));
        for (int i = 0; i < batchSize; ++i) {
            ids.add(changeList.pop(CACHE_CHANGE_LIST));
        }

        for (Long id : ids) {
            postService.updatePostCounting(id, getVoteUp(id), getVoteDown(id), getPostHits(id), getReplies(id));
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Closing CountingService, try to save all counting information.");
        saveCountings(Integer.MAX_VALUE);
    }
}
