package com.expper.service;

import com.expper.domain.Post;
import com.expper.domain.Tag;
import com.expper.domain.Topic;
import com.expper.repository.PostRepository;
import com.expper.web.exceptions.PageNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class PostListService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private HotPostService hotPostService;

    @Autowired
    private NewPostsService newPostsService;

    public static final int MAX_CACHE_POST_PAGES = 20;


    // BEGIN of hot post list

    public List<Post> getHotPostsOfPage(int page, int pageSize) throws PageNotFoundException {
        Set<ZSetOperations.TypedTuple<Long>> idsWithScore = hotPostService.getPageWithScore(page, pageSize);
        return getHotPostsOfPage(page, idsWithScore);
    }

    public List<Post> getHotPostsOfPage(int page, int pageSize, Tag tag) throws PageNotFoundException {
        Set<ZSetOperations.TypedTuple<Long>> idsWithScore = hotPostService.getPageWithScoreOfTag(tag.getId(), page, pageSize);
        return getHotPostsOfPage(page, idsWithScore);
    }

    public List<Post> getHotPostsOfPage(int page, int pageSize, Topic topic) throws PageNotFoundException {
        Set<ZSetOperations.TypedTuple<Long>> idsWithScore = hotPostService.getPageWithScoreOfTopic(topic.getId(), page, pageSize);
        return getHotPostsOfPage(page, idsWithScore);
    }

    private List<Post> getHotPostsOfPage(int page, Set<ZSetOperations.TypedTuple<Long>> idsWithScore) throws PageNotFoundException {
        if (page > 0 && idsWithScore.isEmpty()) {
            throw new PageNotFoundException();
        }

        List<Long> ids = new ArrayList<>();
        idsWithScore.forEach(id -> ids.add(id.getValue()));

        List<Post> posts = new ArrayList<>();
        if (page < MAX_CACHE_POST_PAGES) {
            for (Long id : ids) {
                posts.add(postService.getPost(id));
            }
        } else {
            posts = postRepository.findByIdIn(ids);
            hotPostService.sortByScore(posts, idsWithScore);
        }

        return posts;
    }

    // END ----------------


    // BEGIN of new post list

    public List<Post> getNewPostsOfPage(int page, int pageSize) throws PageNotFoundException {
        Set<Long> ids = newPostsService.getPage(page, pageSize);
        return getNewPostsOfPage(page, ids);
    }

    public List<Post> getNewPostsOfPage(int page, int pageSize, Tag tag) throws PageNotFoundException {
        Set<Long> ids = newPostsService.getPageOfTag(tag.getId(), page, pageSize);
        return getNewPostsOfPage(page, ids);
    }

    public List<Post> getNewPostsOfPage(int page, int pageSize, Topic topic) throws PageNotFoundException {
        Set<Long> ids = newPostsService.getPageOfTopic(topic.getId(), page, pageSize);
        return getNewPostsOfPage(page, ids);
    }

    private List<Post> getNewPostsOfPage(int page, Set<Long> ids) throws PageNotFoundException {
        if (page > 0 && ids.isEmpty()) {
            throw new PageNotFoundException();
        }

        List<Post> posts = new ArrayList<>();

        if (page < MAX_CACHE_POST_PAGES) {
            for (Long id : ids) {
                posts.add(postService.getPost(id));
            }
        } else {
            posts = postRepository.findByIdIn(ids);
            posts.sort((o1, o2) -> {
                double diff = o2.getShareAt().toEpochSecond() - o1.getShareAt().toEpochSecond();
                return diff > 0 ? 1 : (diff < 0 ? -1 : 0);
            });
        }

        return posts;
    }

    // END -----------------
}
