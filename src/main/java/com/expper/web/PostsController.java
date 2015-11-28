package com.expper.web;

import com.codahale.metrics.annotation.Timed;
import com.expper.domain.Post;
import com.expper.domain.Vote;
import com.expper.domain.enumeration.PostStatus;
import com.expper.security.SecurityUtils;
import com.expper.service.NewPostsService;
import com.expper.service.PostListService;
import com.expper.service.PostService;
import com.expper.service.TopicsService;
import com.expper.service.UserService;
import com.expper.service.VoteService;
import com.expper.web.exceptions.PageNotFoundException;
import com.expper.service.CountingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Controller
@RequestMapping("/posts")
public class PostsController {
    @Autowired
    private PostService postService;

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

    @Autowired
    private PostListService postListService;

    private static final int PAGE_SIZE = 20;

    @RequestMapping(value = "new", method = RequestMethod.GET)
    @Timed
    public String newPosts(@RequestParam(defaultValue = "1") int page, Model model) {
        page = page < 1 ? 1 : page - 1;
        long size = newPostsService.size();
        long pages = size / PAGE_SIZE + (size % PAGE_SIZE != 0 ? 1 : 0);

        List<Post> posts = postListService.getNewPostsOfPage(page, PAGE_SIZE);

        model.addAttribute("topics", topicsService.getAll());
        model.addAttribute("counting", countingService.getPostListCounting(posts));
        model.addAttribute("page", page + 1);
        model.addAttribute("totalPages", pages);
        model.addAttribute("posts", posts);
        model.addAttribute("votes", voteService.getCurrentUserVoteMapFor(posts));

        return "posts/new";
    }

    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.GET)
    @Timed
    public String showPost(@PathVariable Long id, Model model) {
        Post post = postService.getPost(id);

        if (post == null || post.getStatus() != PostStatus.PUBLIC) {
            throw new PageNotFoundException("没有找到文章(" + id + ")");
        }

        countingService.incPostHits(id);

        Vote vote = null;
        if (SecurityUtils.isAuthenticated()) {
            vote = voteService.checkUserVote(id, userService.getCurrentUserId());
        }

        model.addAttribute("vote", vote);
        model.addAttribute("post", post);
        model.addAttribute("counting", countingService.getPostCounting(id));
        return "posts/show";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    @Timed
    public String search(@RequestParam(defaultValue = "1") int page, Model model) {
        page = page < 1 ? 1 : page - 1;

        return "posts/search";
    }
}
