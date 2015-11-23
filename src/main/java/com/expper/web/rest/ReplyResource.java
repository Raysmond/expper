package com.expper.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.expper.domain.Reply;
import com.expper.domain.enumeration.ReplyStatus;
import com.expper.repository.ReplyRepository;
import com.expper.security.SecurityUtils;
import com.expper.service.ReplyService;
import com.expper.service.UserService;
import com.expper.web.rest.util.HeaderUtil;
import com.expper.web.rest.util.PaginationUtil;
import com.expper.web.rest.dto.ReplyDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ReplyResource {

    private final Logger log = LoggerFactory.getLogger(ReplyResource.class);

    @Inject
    private ReplyRepository replyRepository;

    @Inject
    private ReplyService replyService;

    @Autowired
    private UserService userService;

    /**
     * POST  /replys -> Create a new reply.
     */
    @RequestMapping(value = "/posts/{postId:\\d+}/replies",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ReplyDTO> createReply(@Valid @RequestBody ReplyDTO replyDTO, @PathVariable Long postId) throws URISyntaxException {
        log.debug("REST request to save Reply : {}", replyDTO);
        if (replyDTO.getId() != null || replyDTO.getUserId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new reply cannot already have an ID or an userId").body(null);
        }
        Reply result = replyService.createReply(replyDTO, userService.getCurrentUser());
        return ResponseEntity.created(new URI("/api/posts/" + postId + "/replies/" + result.getId()))
            .headers(HeaderUtil.addMessage("Add reply successfully", result.getId().toString()))
            .body(new ReplyDTO(result));
    }

    /**
     * PUT  /replys -> Updates an existing reply.
     */
    @RequestMapping(value = "/posts/{postId:\\d+}/replies",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ReplyDTO> updateReply(@Valid @RequestBody ReplyDTO replyDTO, @PathVariable Long postId) throws URISyntaxException {
        log.debug("REST request to update Reply : {}", replyDTO);
        if (replyDTO.getId() == null) {
            return createReply(replyDTO, postId);
        }
        Reply reply = replyDTO.toReply();
        Reply result = replyRepository.save(reply);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("reply", replyDTO.getId().toString()))
            .body(new ReplyDTO(result));
    }

    @RequestMapping(value = "/posts/{postId:\\d+}/replies/all",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<ReplyDTO>> getAllReplies(Pageable pageable, @PathVariable Long postId)
        throws URISyntaxException {
        Page<Reply> page = replyRepository.findAllByPost(pageable, postId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/posts/" + postId + "/replies");
        return new ResponseEntity<>(page.getContent().stream()
            .map(ReplyDTO::new)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /replys/:id -> get the "id" reply.
     */
    @RequestMapping(value = "/replies/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ReplyDTO> getReply(@PathVariable Long id) {
        log.debug("REST request to get Reply : {}", id);
        return Optional.ofNullable(replyRepository.findOne(id))
            .map(ReplyDTO::new)
            .map(replyDTO -> new ResponseEntity<>(
                replyDTO,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /replys/:id -> delete the "id" reply.
     */
    @RequestMapping(value = "/replies/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteReply(@PathVariable Long id) {
        log.debug("REST request to delete Reply : {}", id);
        replyService.deleteReply(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("reply", id.toString())).build();
    }
}
