package com.expper.web;

import com.codahale.metrics.annotation.Timed;
import com.expper.domain.Post;
import com.expper.domain.Topic;
import com.expper.service.CountingService;
import com.expper.service.HotPostService;
import com.expper.service.NewPostsService;
import com.expper.service.PostListService;
import com.expper.service.TopicsService;
import com.expper.service.VoteService;
import com.expper.web.exceptions.PageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Controller
@RequestMapping("/topics")
public class TopicsController {

    @Autowired
    private TopicsService topicsService;

    @Autowired
    private HotPostService hotPostService;

    @Autowired
    private NewPostsService newPostsService;

    @Autowired
    private CountingService countingService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private PostListService postListService;

    public static final int PAGE_SIZE = 20;

    @RequestMapping(value = "", method = GET)
    @Timed
    public String index(Model model) {
        List<Topic> topics = topicsService.getAll();
        model.addAttribute("topics", topics);
        return "topics/index";
    }

    @RequestMapping(value = "{name}", method = GET)
    @Timed
    public String topic(@PathVariable String name, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "hot") String sort, Model model) {
        Topic topic = topicsService.getTopicByName(name);

        if (topic == null) {
            throw new PageNotFoundException();
        }

        page = page < 1 ? 1 : page - 1;
        long size = 0;
        List<Post> posts = new ArrayList<>();

        if (sort.equals("hot")) {
            size = hotPostService.sizeOfTopic(topic.getId());
            posts = postListService.getHotPostsOfPage(page, PAGE_SIZE, topic);
        }

        if (sort.equals("new")) {
            size = newPostsService.sizeOfTopicList(topic.getId());
            posts = postListService.getNewPostsOfPage(page, PAGE_SIZE, topic);
        }

        long pages = size / PAGE_SIZE + (size % PAGE_SIZE != 0 ? 1 : 0);
        model.addAttribute("sort", sort);
        model.addAttribute("topic", topic);
        model.addAttribute("counting", countingService.getPostListCounting(posts));
        model.addAttribute("page", page + 1);
        model.addAttribute("totalPages", pages);
        model.addAttribute("posts", posts);
        model.addAttribute("votes", voteService.getCurrentUserVoteMapFor(posts));

        return "topics/show";
    }
}
