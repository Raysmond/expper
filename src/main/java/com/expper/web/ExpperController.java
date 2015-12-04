package com.expper.web;

import com.codahale.metrics.annotation.Timed;
import com.expper.domain.Post;
import com.expper.security.SecurityUtils;
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

    @RequestMapping(value = "", method = GET)
    @Timed
    public String index() {
        if (SecurityUtils.isAuthenticated()){
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
    public String about() {
        return "about";
    }
}
