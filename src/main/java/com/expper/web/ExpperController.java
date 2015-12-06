package com.expper.web;

import com.codahale.metrics.annotation.Timed;
import com.expper.domain.Content;
import com.expper.domain.Post;
import com.expper.repository.ContentRepository;
import com.expper.security.SecurityUtils;
import com.expper.service.ContentService;
import com.expper.service.HotPostService;
import com.expper.service.PostListService;
import com.expper.service.VoteService;
import com.expper.service.CountingService;
import com.expper.web.exceptions.PageNotFoundException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Controller
public class ExpperController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private ContentRepository contentRepository;

    @RequestMapping(value = "", method = GET)
    @Timed
    public String index() {
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/me";
        }

        return "leading_home";
    }

    @RequestMapping(value = "me", method = GET)
    @Timed
    public String userDashboard() {
        return "user_dashboard";
    }

    @RequestMapping(value = "about", method = GET)
    public String about(Model model) {
        Content content = contentService.getPublishedContent("about");

        if (content == null) {
            content = new Content();
            content.setPermalink("about");
            content.setTitle("About");
            content.setContent("todo");
            content = contentRepository.save(content);
        }

        model.addAttribute("content", content);
        return "about";
    }

    @RequestMapping(value = "p/{permalink}", method = GET)
    public String page(@PathVariable String permalink, Model model) {
        Content content = contentService.getPublishedContent(permalink);

        if (content == null) {
            throw new PageNotFoundException();
        }

        model.addAttribute("content", content);
        return "content/page";
    }
}
