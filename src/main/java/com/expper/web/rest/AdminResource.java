package com.expper.web.rest;

import com.expper.service.search.PostSearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Raysmond<i@raysmond.com>
 */
@RestController
@RequestMapping("/api/admin")
public class AdminResource {

    private final Logger log = LoggerFactory.getLogger(AdminResource.class);

    @Autowired
    private PostSearchService postSearchService;


    @RequestMapping(value = "posts/index_all", method = RequestMethod.GET)
    public ResponseEntity<String> indexAllPosts() {
        postSearchService.indexAll();
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
