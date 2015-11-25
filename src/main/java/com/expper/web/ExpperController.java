package com.expper.web;

import com.codahale.metrics.annotation.Timed;
import com.expper.domain.Post;
import com.expper.service.HotPostService;
import com.expper.service.PostListService;
import com.expper.service.VoteService;
import com.expper.service.CountingService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
    private VoteService voteService;

    @Autowired
    private HotPostService hotPostService;

    @Autowired
    private PostListService postListService;

    @Autowired
    private CountingService countingService;

    private static final int PAGE_SIZE = 20;

    @RequestMapping(value = "", method = GET)
    @Timed
    @Transactional(readOnly = true)
    public String index(@RequestParam(defaultValue = "1") int page, Model model) {
        page = page < 1 ? 1 : page - 1;
        long size = hotPostService.size();
        long pages = size / PAGE_SIZE + (size % PAGE_SIZE != 0 ? 1 : 0);

        List<Post> posts = postListService.getHotPostsOfPage(page, PAGE_SIZE);

        model.addAttribute("counting", countingService.getPostListCounting(posts));
        model.addAttribute("page", page + 1);
        model.addAttribute("totalPages", pages);
        model.addAttribute("posts", posts);
        model.addAttribute("votes", voteService.getCurrentUserVoteMapFor(posts));

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
