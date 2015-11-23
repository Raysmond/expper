package com.expper.web;

import com.expper.domain.Post;
import com.expper.domain.User;
import com.expper.domain.Vote;
import com.expper.domain.enumeration.PostStatus;
import com.expper.repository.PostRepository;
import com.expper.repository.UserRepository;
import com.expper.service.CountingService;
import com.expper.service.UserService;
import com.expper.web.exceptions.PageNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Controller
public class UsersController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CountingService countingService;

    @RequestMapping(value = "/u/{username}", method = RequestMethod.GET)
    public String show(@PathVariable String username, Model model) {
        User user = userRepository.findByLogin(username);

        if (user == null) {
            throw new PageNotFoundException("User " + username + " is not found.");
        }

        Page<Post> posts = postRepository.findUserPostsByStatus(user.getId(), PostStatus.PUBLIC, new PageRequest(0, 20));

        List<Long> ids = new ArrayList<>();
        posts.forEach(post -> ids.add(post.getId()));

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        model.addAttribute("votes", new HashMap<Long, Vote>());
        model.addAttribute("counting", countingService.getPostListCounting(ids));

        return "users/show";
    }
}
