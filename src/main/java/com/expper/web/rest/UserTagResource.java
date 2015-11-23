package com.expper.web.rest;

import com.expper.domain.Tag;
import com.expper.repository.TagRepository;
import com.expper.security.SecurityUtils;
import com.expper.service.TagService;
import com.expper.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Raysmond<i@raysmond.com>
 */
@RestController
@RequestMapping("/api/tags")
public class UserTagResource {
    @Autowired
    private TagService tagService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Tag>> search(@RequestParam String name) {
        return new ResponseEntity<>(
            tagRepository.searchTags(name.trim().toUpperCase(), new PageRequest(0, 10)).getContent(),
            HttpStatus.OK);
    }

    @RequestMapping(value = "{id:\\d+}/follow", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> followTag(@PathVariable Long id) {
        Tag tag = tagRepository.findOne(id);
        if (tag == null) {
            return new ResponseEntity<>("Tag " + id + " is not found.", HttpStatus.NOT_FOUND);
        } else {
            Long followersCount = tagService.followTag(userService.getCurrentUserId(), tag);
            return new ResponseEntity<>("{\"followers_count\":" + followersCount + "}", HttpStatus.OK);
        }
    }

    @RequestMapping(value = "{id:\\d+}/unfollow", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> unFollowTag(@PathVariable Long id) {
        Tag tag = tagRepository.findOne(id);
        if (tag == null) {
            return new ResponseEntity<>("Tag " + id + " is not found.", HttpStatus.NOT_FOUND);
        } else {
            Long followersCount = tagService.unFollowTag(userService.getCurrentUserId(), tag);
            return new ResponseEntity<>("{\"followers_count\":" + followersCount + "}", HttpStatus.OK);
        }
    }
}
