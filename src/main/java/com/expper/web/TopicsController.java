package com.expper.web;

import com.codahale.metrics.Counting;
import com.codahale.metrics.annotation.Timed;
import com.expper.domain.Post;
import com.expper.domain.Topic;
import com.expper.domain.Vote;
import com.expper.repository.PostRepository;
import com.expper.repository.TagRepository;
import com.expper.security.SecurityUtils;
import com.expper.service.CountingService;
import com.expper.service.HotPostService;
import com.expper.service.NewPostsService;
import com.expper.service.PostService;
import com.expper.service.TopicsService;
import com.expper.service.UserService;
import com.expper.service.VoteService;
import com.expper.web.exceptions.PageNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private PostRepository postRepository;

    @Autowired
    private CountingService countingService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

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
        long pages = 0;

        Collection<Long> ids = new ArrayList<>();
        List<Post> posts = new ArrayList<>();

        if (sort.equals("hot")) {
            size = hotPostService.sizeOfTopic(topic.getId());

            Set<ZSetOperations.TypedTuple<Long>> idsWithScore = hotPostService.getPageWithScoreOfTopic(topic.getId(), page, PAGE_SIZE);
            if (!idsWithScore.isEmpty()) {
                for (ZSetOperations.TypedTuple<Long> tuple : idsWithScore) {
                    ids.add(tuple.getValue());
                }

                if (page < 5) {
                    for (Long id : ids)
                        posts.add(postService.getPost(id));
                } else {
                    posts = postRepository.findByIdIn(ids);
                    hotPostService.sortByScore(posts, idsWithScore);
                }
            }
        }

        if (sort.equals("new")) {
            size = newPostsService.sizeOfTopicList(topic.getId());
            ids = newPostsService.getPageOfTopic(topic.getId(), page, PAGE_SIZE);

            if (!ids.isEmpty()) {
                if (page < 5) {
                    for (Long id : ids)
                        posts.add(postService.getPost(id));
                } else {
                    posts = postRepository.findByIdIn(ids);
                }
            }
        }

        pages = size / PAGE_SIZE + (size % PAGE_SIZE != 0 ? 1 : 0);
        model.addAttribute("sort", sort);
        model.addAttribute("topic", topic);
        model.addAttribute("counting", countingService.getPostListCounting(ids));
        model.addAttribute("page", page + 1);
        model.addAttribute("totalPages", pages);
        model.addAttribute("posts", posts);

        Map<Long, Vote> userVotesMap = new HashMap<>();
        if (SecurityUtils.isAuthenticated()) {
            Long userId = userService.getCurrentUserId();
            userVotesMap = voteService.getUserVoteMapFor(posts, userId);
        }
        model.addAttribute("votes", userVotesMap);

        return "topics/show";
    }
}
