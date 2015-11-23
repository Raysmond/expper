package com.expper.web;

import com.codahale.metrics.annotation.Timed;
import com.expper.domain.Post;
import com.expper.domain.Vote;
import com.expper.repository.PostRepository;
import com.expper.security.SecurityUtils;
import com.expper.service.HotPostService;
import com.expper.service.PostService;
import com.expper.service.TagService;
import com.expper.service.TopicsService;
import com.expper.service.UserService;
import com.expper.service.VoteService;
import com.expper.service.CountingService;
import com.expper.web.exceptions.PageNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Controller
public class ExpperController {

    @Autowired
    private PostService postService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private HotPostService hotPostService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CountingService countingService;

    @Autowired
    private UserService userService;

    private static final int PAGE_SIZE = 20;

    @RequestMapping(value = "", method = GET)
    @Timed
    @Transactional(readOnly = true)
    public String index(@RequestParam(defaultValue = "1") int page, Model model) {
        page = page < 1 ? 1 : page - 1;

        long size = hotPostService.size();
        if (size == 0) {
            hotPostService.init();
            size = hotPostService.size();
        }

        long pages = size / PAGE_SIZE + (size % PAGE_SIZE != 0 ? 1 : 0);

        Set<ZSetOperations.TypedTuple<Long>> idsWithScore = hotPostService.getPageWithScore(page, PAGE_SIZE);
        List<Long> ids = new ArrayList<>();
        List<Post> posts = new ArrayList<>();

        idsWithScore.forEach(id -> ids.add(id.getValue()));

        if (ids.isEmpty()){
            throw new PageNotFoundException();
        }

        // Cache the first 10 pages
        if (page < 10) {
            for (Long id : ids)
                posts.add(postService.getPost(id));
        } else {
            posts = postRepository.findByIdIn(ids);
            hotPostService.sortByScore(posts, idsWithScore);
        }

        model.addAttribute("counting", countingService.getPostListCounting(ids));
        model.addAttribute("page", page + 1);
        model.addAttribute("totalPages", pages);
        model.addAttribute("posts", posts);

        if (SecurityUtils.isAuthenticated()) {
            model.addAttribute("votes", voteService.getUserVoteMapFor(posts, userService.getCurrentUserId()));
        } else {
            model.addAttribute("votes", new HashMap<Long, Vote>());
        }

        return "community/index";
    }

    @RequestMapping(value = "me", method = GET)
    @Timed
    public String userDashboard() {
        return "user_dashboard";
    }

    @RequestMapping(value = "about", method = GET)
    public String about() {
        return "about";
    }
}
