package com.expper.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.expper.domain.Topic;
import com.expper.repository.TopicRepository;
import com.expper.service.TopicsService;
import com.expper.web.rest.util.HeaderUtil;
import com.expper.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Topic.
 */
@RestController
@RequestMapping("/api")
public class TopicResource {

    private final Logger log = LoggerFactory.getLogger(TopicResource.class);

    @Inject
    private TopicRepository topicRepository;

    @Inject
    private TopicsService topicsService;

    /**
     * POST  /topics -> Create a new topic.
     */
    @RequestMapping(value = "/topics",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Topic> createTopic(@Valid @RequestBody Topic topic) throws URISyntaxException {
        log.debug("REST request to save Topic : {}", topic);
        if (topic.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new topic cannot already have an ID").body(null);
        }
        Topic result = topicsService.createTopic(topic);
        return ResponseEntity.created(new URI("/api/topics/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("topic", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /topics -> Updates an existing topic.
     */
    @RequestMapping(value = "/topics",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Topic> updateTopic(@Valid @RequestBody Topic topic) throws URISyntaxException {
        log.debug("REST request to update Topic : {}", topic);
        if (topic.getId() == null) {
            return createTopic(topic);
        }
        Topic result = topicsService.updateTopic(topic);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("topic", topic.getId().toString()))
            .body(result);
    }

    /**
     * GET  /topics -> get all the topics.
     */
    @RequestMapping(value = "/topics",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Topic>> getAllTopics(Pageable pageable)
        throws URISyntaxException {
        Pageable pageRequest = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC, "weight");
        Page<Topic> page = topicRepository.findAll(pageRequest);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/topics");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /topics/:id -> get the "id" topic.
     */
    @RequestMapping(value = "/topics/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Topic> getTopic(@PathVariable Long id) {
        log.debug("REST request to get Topic : {}", id);
        return Optional.ofNullable(topicRepository.findOneWithEagerRelationships(id))
            .map(topic -> new ResponseEntity<>(
                topic,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /topics/:id -> delete the "id" topic.
     */
    @RequestMapping(value = "/topics/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        log.debug("REST request to delete Topic : {}", id);
        topicRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("topic", id.toString())).build();
    }
}
