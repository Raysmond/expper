package com.expper.web;

import com.codahale.metrics.annotation.Timed;
import com.expper.domain.Post;
import com.expper.domain.Vote;
import com.expper.domain.enumeration.PostStatus;
import com.expper.repository.PostRepository;
import com.expper.security.SecurityUtils;
import com.expper.service.NewPostsService;
import com.expper.service.PostService;
import com.expper.service.TagService;
import com.expper.service.TopicsService;
import com.expper.service.UserService;
import com.expper.service.VoteService;
import com.expper.service.support.PostCounting;
import com.expper.web.exceptions.PageNotFoundException;
import com.expper.service.CountingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Controller
@RequestMapping("/posts")
public class PostsController {
    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CountingService countingService;

    @Autowired
    private NewPostsService newPostsService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private TopicsService topicsService;

    @Autowired
    private UserService userService;

    private static final int PAGE_SIZE = 20;

    @RequestMapping(value = "new", method = RequestMethod.GET)
    @Timed
    public String newPosts(@RequestParam(defaultValue = "1") int page, Model model) {
        page = page < 1 ? 1 : page - 1;

        long size = newPostsService.size();
        if (size == 0) {
            newPostsService.init();
            size = newPostsService.size();
        }

        long pages = size / PAGE_SIZE + (size % PAGE_SIZE != 0 ? 1 : 0);

        Collection<Long> ids = newPostsService.getPage(page, PAGE_SIZE);
        List<Post> posts = new ArrayList<>();

        if (ids.isEmpty()) {
            throw new PageNotFoundException();
        }

        // Cache the first 10 pages
        if (page < 10) {
            posts.addAll(ids.stream().map(postService::getPost).collect(Collectors.toList()));
        } else {
            posts = postRepository.findByIdIn(ids);
        }

        model.addAttribute("topics", topicsService.getAll());
        model.addAttribute("counting", countingService.getPostListCounting(ids));
        model.addAttribute("page", page + 1);
        model.addAttribute("totalPages", pages);
        model.addAttribute("posts", posts);

        if (SecurityUtils.isAuthenticated()) {
            model.addAttribute("votes", voteService.getUserVoteMap(ids, userService.getCurrentUserId()));
        } else {
            model.addAttribute("votes", new HashMap<Long, Vote>());
        }

        return "posts/new";
    }

    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.GET)
    @Timed
    public String showPost(@PathVariable Long id, Model model) {
        Post post = postService.getPost(id);

        if (post == null || post.getStatus() != PostStatus.PUBLIC) {
            throw new PageNotFoundException("没有找到文章(" + id + ")");
        }

        // Increment post hits
        countingService.incPostHits(id);
        PostCounting postCounting = countingService.getPostCounting(id);

        Vote vote = null;
        if (SecurityUtils.isAuthenticated()) {
            // TODO check whether the user has voted for the post
            vote = voteService.checkUserVote(id, userService.getCurrentUserId());
        }

        model.addAttribute("vote", vote);
        model.addAttribute("post", post);
        model.addAttribute("counting", postCounting);
        return "posts/show";
    }
}
