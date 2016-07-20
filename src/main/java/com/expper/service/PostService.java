package com.expper.service;

import com.codahale.metrics.annotation.Timed;
import com.expper.config.RabbitmqConfiguration;
import com.expper.domain.Post;
import com.expper.domain.Tag;
import com.expper.domain.User;
import com.expper.domain.enumeration.PostStatus;
import com.expper.repository.PostRepository;
import com.expper.repository.TagRepository;
import com.expper.repository.search.PostSearchRepository;
import com.expper.service.search.PostSearchService;
import com.expper.service.util.StringUtil;
import com.expper.web.rest.dto.PostDTO;
import com.expper.web.rest.dto.PostTagCountDTO;
import com.expper.web.rest.mapper.PostMapper;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private HotPostService hotPostService;

    @Autowired
    private NewPostsService newPostsService;

    @Autowired
    private CountingService countingService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostSearchService PostSearchService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static final String SERVER_URL = "http://10.131.245.176:8000/get?url=%s";

    public static final String CACHE_POST = "entity.post";

    public static final String CACHE_COUNT_USER_TAG_POSTS = "count.user.tags_posts";

    @Cacheable(value = CACHE_POST, key = "#id.toString()")
    public Post getPost(Long id) {
        return postRepository.findPostWithTags(id);
    }

    @Cacheable(value = TagService.CACHE_COUNT_USER, key = "#userId.toString().concat('_posts_count')")
    public Long getUserPostsCount(Long userId) {
        return postRepository.countUserPosts(userId);
    }

    @Transactional
    @Timed
    @Caching(evict = {
        @CacheEvict(value = TagService.CACHE_COUNT_USER, key = "#user.id.toString().concat('_posts_count')"),
        @CacheEvict(value = CACHE_COUNT_USER_TAG_POSTS, key = "#user.id.toString().concat('_tags_posts_count')", allEntries = true)
    })
    public Optional<Post> asyncCreatePost(PostDTO postDTO, User user) throws JSONException, URISyntaxException {
        Post post = postMapper.postDTOToPost(postDTO);
        postDTO.setUserId(user.getId());

        boolean hasTitle = postDTO.getTitle() != null && !postDTO.getTitle().trim().isEmpty();

        if (!hasTitle) {
            rabbitTemplate.convertAndSend(RabbitmqConfiguration.QUEUE_CREATE_POST, postDTO);
        } else {
            // Save the post(with content) to database first, then send it to RabbitMQ queue
            // to get the full content of the post
            post.setDomain(StringUtil.getDomainName(post.getUrl()));
            post.setUser(user);
            updateTags(post, null);

            // Invoked after the RabbitMQ finishing getting the full text content
            // saveNewPost(post);

            postRepository.save(post);
            postRepository.flush();
            PostSearchService.index(post);

            rabbitTemplate.convertAndSend(RabbitmqConfiguration.QUEUE_GET_ARTICLE, post);
        }

        return Optional.of(post);
    }

    @Transactional
    @Timed
    @Caching(evict = {
        @CacheEvict(value = TagService.CACHE_COUNT_USER, key = "#postDTO.userId.toString().concat('_posts_count')"),
        @CacheEvict(value = CACHE_COUNT_USER_TAG_POSTS, key = "#postDTO.userId.toString().concat('_tags_posts_count')", allEntries = true),
    })
    public Optional<Post> createPost(PostDTO postDTO) throws JSONException {
        Post post = postMapper.postDTOToPost(postDTO);
        String result = getWebPost(String.format(SERVER_URL, post.getUrl()));

        if (result == null)
            return Optional.empty();

        JSONObject json = new JSONObject(result);
        String content = json.getString("content");

        // Filter html tags
        content = Jsoup.clean(content, Whitelist.relaxed());

        post.setTitle(json.getString("title"));
        post.setTitle(post.getTitle().substring(0, Math.min(255, post.getTitle().length())));
        post.setContent(content);
        post.setDomain(json.getString("host"));

        updateTags(post, null);
        saveNewPost(post);

        return Optional.of(post);
    }

    /**
     * Only used when creating a new post
     */
    public void saveNewPost(Post post) {
        postRepository.save(post);
        PostSearchService.index(post);

        if (post.getStatus() == PostStatus.PUBLIC) {
            hotPostService.addHotPost(post);
            hotPostService.addTaggedPost(post, post.getTags());
            newPostsService.add(post);
            newPostsService.addTaggedPost(post, post.getTags());
            countingService.incPublicPostsCount();
        }

        countingService.incPostsCount();
    }


    /**
     * Get and parse web article page
     */
    public String getWebPost(String url) {
        RestTemplate rest = new RestTemplate();

        return rest.getForObject(url, String.class);
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_POST, key = "#post.id.toString()"),
        @CacheEvict(value = CACHE_COUNT_USER_TAG_POSTS, key = "#post.user.id.toString().concat('_tags_posts_count')", allEntries = true)
    })
    @Transactional
    public Post updatePost(Post post) {
        Post oldPost = postRepository.findPostWithTags(post.getId());
        PostStatus oldStatus = oldPost.getStatus();

        // Delete from hot post set of related tags
        if (oldStatus == PostStatus.PUBLIC) {
            hotPostService.removeTaggedPost(oldPost, oldPost.getTags());
            newPostsService.removeTaggedPost(oldPost.getId(), oldPost.getTags());
        }

        updateTags(post, oldPost);

        post.setContent(Jsoup.clean(post.getContent(), Whitelist.relaxed()));

        Post result = postRepository.save(post);
        postRepository.flush();
        result.getTags().size();

        PostSearchService.index(result);

        if (oldStatus == PostStatus.PRIVATE && result.getStatus() == PostStatus.PUBLIC) {
            newPostsService.add(result);
            hotPostService.addHotPost(result);
            countingService.incPublicPostsCount();
        }

        if (oldStatus == PostStatus.PUBLIC && result.getStatus() == PostStatus.PRIVATE) {
            newPostsService.remove(result.getId());
            hotPostService.removeHotPost(result);
            countingService.decPublicPostsCount();
        }

        if (post.getStatus() == PostStatus.PUBLIC) {
            hotPostService.addTaggedPost(result, result.getTags());
            newPostsService.addTaggedPost(result, result.getTags());
        }

        return result;
    }


    @Caching(evict = {
        @CacheEvict(value = CACHE_POST, key = "#post.id.toString()"),
        @CacheEvict(value = CACHE_COUNT_USER_TAG_POSTS, key = "#post.user.id.toString().concat('_tags_posts_count')", allEntries = true),
        @CacheEvict(value = TagService.CACHE_COUNT_USER, key = "#post.user.id.toString().concat('_posts_count')")
    })
    @Transactional
    public void deletePost(Post post) {
        PostStatus status = post.getStatus();
        postRepository.delete(post.getId());

        if (status == PostStatus.PUBLIC) {
            Set<Tag> tags = tagRepository.findPostTags(post.getId());
            hotPostService.removeHotPost(post);
            hotPostService.removeTaggedPost(post, tags);
            newPostsService.remove(post.getId());
            newPostsService.removeTaggedPost(post.getId(), tags);
            countingService.decPublicPostsCount();
            tags.forEach(tagService::decreasePostCountByOne); // 标签文章统计需要减一
        }

        PostSearchService.deleteIndex(post.getId());
        countingService.decPostsCount();
    }

    @Timed
    public void updateTags(Post post, Post oldPost) {
        Set<Tag> oldTags = new HashSet<>();

        if (oldPost != null) {
            oldTags = oldPost.getTags();
        }

        boolean toPublicPost = (oldPost == null || oldPost.getStatus() == PostStatus.PRIVATE) && post.getStatus() == PostStatus.PUBLIC;
        boolean toPrivatePost = oldPost != null && oldPost.getStatus() == PostStatus.PUBLIC && post.getStatus() == PostStatus.PRIVATE;

        Set<Tag> tags = new HashSet<>();
        post.getTags().forEach(tag -> {
            if (tag.getId() == null || tag.getId() == 0L) {
                tag = tagService.findOrCreateByName(post, tag.getFriendlyName());

                // 添加新的标签，只要现在状态为PUBLIC，就要加一
                if (post.getStatus() == PostStatus.PUBLIC) {
                    tagService.increasePostCountByOne(tag);
                }
            } else {
                // 原来就添加了该标签，现在如果状态由PUBLIC变为PRIVATE，则减一
                if (toPrivatePost) {
                    tagService.decreasePostCountByOne(tag);
                }

                // 原来就添加了该标签，现在如果状态由PRIVATE变为PUBLIC，则加一
                if (toPublicPost) {
                    tagService.increasePostCountByOne(tag);
                }
            }

            tags.add(tag);
        });

        post.setTags(tags);

        // Decrease tag posts count
        oldTags.forEach(tag -> {
            if (!tags.contains(tag)) {
                // 如果原来的状态为PUBLIC，现在删除标签，则要减一
                if (oldPost != null && oldPost.getStatus() == PostStatus.PUBLIC) {
                    tagService.decreasePostCountByOne(tag);
                }
            }
        });
    }

    @Cacheable(value = CACHE_COUNT_USER_TAG_POSTS, key = "#userId.toString().concat('_tags_posts_count')")
    public List<PostTagCountDTO> countUserPostsByTags(Long userId) {
        List<Object[]> counts = postRepository.countUserPostsByTags(userId);
        List<PostTagCountDTO> result = new ArrayList<>();
        counts.forEach(count -> result.add(new PostTagCountDTO((Long) count[0], (Tag) count[1])));
        return result;
    }

    @Transactional
    public void updatePostCounting(long postId, int votesUp, int votesDown, int hits, int replies) {
        postRepository.updateCounting(postId, votesUp, votesDown, hits, replies);
    }
}
