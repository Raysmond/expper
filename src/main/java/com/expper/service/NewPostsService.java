package com.expper.service;

import com.expper.domain.Post;
import com.expper.domain.Tag;
import com.expper.domain.Topic;
import com.expper.domain.enumeration.PostStatus;
import com.expper.repository.PostRepository;
import com.expper.repository.TagRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class NewPostsService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TopicsService topicsService;

    @Resource(name = "redisListTemplate")
    ZSetOperations<String, Long> newPosts;

    public static final String CACHE_NEW_POST = "new_post_ids";

    public static final String CACHE_NEW_TAG_POST = "new_post_ids_tag_";

    public static final String CACHE_NEW_TOPIC_POST = "new_post_ids_topic_";

    public Set<Long> getPage(int page, int pageSize) {
        return newPosts.reverseRange(CACHE_NEW_POST, (long) page * pageSize, (page + 1) * pageSize - 1);
    }

    public void add(Post post) {
        newPosts.add(CACHE_NEW_POST, post.getId(), post.getShareAt().toEpochSecond());
    }

    public void remove(Long postId) {
        newPosts.remove(CACHE_NEW_POST, 1, postId);
    }

    public Long size() {
        return newPosts.size(CACHE_NEW_POST);
    }

    public Long sizeOfTagList(Long tagId) {
        return newPosts.size(CACHE_NEW_TAG_POST + tagId);
    }

    public Long sizeOfTopicList(Long topicId) {
        return newPosts.size((CACHE_NEW_TOPIC_POST + topicId));
    }

    public void addTaggedPost(Post post, Set<Tag> tags) {
        List<Topic> topics = topicsService.getAll();
        tags.forEach(tag -> {
            long seconds = post.getShareAt().toEpochSecond();
            newPosts.add(CACHE_NEW_TAG_POST + tag.getId(), post.getId(), seconds);

            topics.forEach(topic -> {
                if (topic.getTags().contains(tag)) {
                    newPosts.add(CACHE_NEW_TOPIC_POST + topic.getId(), post.getId(), seconds);
                }
            });
        });
    }

    public void removeTaggedPost(Long postId, Set<Tag> tags) {
        List<Topic> topics = topicsService.getAll();
        tags.forEach(tag -> {
            newPosts.remove(CACHE_NEW_TAG_POST + tag.getId(), 1, postId);

            topics.forEach(topic -> {
                if (topic.getTags().contains(tag)) {
                    newPosts.remove(CACHE_NEW_TOPIC_POST + topic.getId(), 1, postId);
                }
            });
        });
    }

    public Set<Long> getPageOfTag(Long tagId, int page, int pageSize) {
        return newPosts.reverseRange(CACHE_NEW_TAG_POST + tagId, (long) page * pageSize, (page + 1) * pageSize - 1);
    }

    public Set<Long> getPageOfTopic(Long topicId, int page, int pageSize) {
        return newPosts.reverseRange(CACHE_NEW_TOPIC_POST + topicId, (long) page * pageSize, (page + 1) * pageSize - 1);
    }

    @PostConstruct
    public void init() {
        if (size() == 0) {
            initNewPosts();
        }
    }

    @Transactional(readOnly = true)
    public void initNewPosts() {
        // Load all post ids from database to Redis
        PageRequest page = new PageRequest(0, Integer.MAX_VALUE, Sort.Direction.ASC, "id");
        Page<Post> posts = postRepository.findPublicPosts(page, PostStatus.PUBLIC);
        posts.forEach(post -> {
            this.add(post);
            this.addTaggedPost(post, tagRepository.findPostTags(post.getId()));
        });
    }

}
