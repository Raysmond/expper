package com.expper.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.expper.config.Constants;
import com.expper.domain.Authority;
import com.expper.domain.Post;
import com.expper.domain.Tag;
import com.expper.domain.User;
import com.expper.domain.enumeration.PostStatus;
import com.expper.repository.PostRepository;
import com.expper.repository.TagRepository;
import com.expper.repository.search.PostSearchRepository;
import com.expper.security.AuthoritiesConstants;
import com.expper.security.SecurityUtils;
import com.expper.service.PostService;
import com.expper.service.UserService;
import com.expper.service.VoteService;
import com.expper.web.rest.dto.PostTagCountDTO;
import com.expper.web.rest.dto.PostVoteDTO;
import com.expper.web.rest.util.HeaderUtil;
import com.expper.web.rest.util.PaginationUtil;
import com.expper.web.rest.dto.PostDTO;
import com.expper.web.rest.mapper.PostMapper;

import org.json.JSONException;
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

import javax.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for managing Post.
 */
@RestController
@RequestMapping("/api")
public class PostResource {

    private final Logger log = LoggerFactory.getLogger(PostResource.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostService postService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserService userService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostSearchRepository postSearchRepository;

    /**
     * POST  /posts -> Create a new post.
     */
    @RequestMapping(value = "/posts",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody PostDTO postDTO) throws URISyntaxException, JSONException {
        log.debug("REST request to save Post : {}", postDTO);
        if (postDTO.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new post cannot already have an ID").body(null);
        }
        Post result = postService.asyncCreatePost(postDTO, userService.getUserWithAuthorities()).get();

        if (result.getId() != null) {
            return ResponseEntity.created(new URI("/api/posts/" + result.getId()))
                .headers(HeaderUtil.createAlert("A new post is created with id " + result.getId(), result.getId().toString()))
                .body(postMapper.postToPostDTO(result));
        } else {
            return ResponseEntity.ok()
                .headers(HeaderUtil.createAlert("The web article will be delivered to you in a moment. Please wait and refresh the page.", null))
                .body(postMapper.postToPostDTO(result));
        }
    }

    /**
     * PUT  /posts -> Updates an existing post.
     */
    @RequestMapping(value = "/posts",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PostDTO> updatePost(@Valid @RequestBody PostDTO postDTO) throws URISyntaxException, JSONException {
        log.debug("REST request to update Post : {}", postDTO.getId());
        if (postDTO.getId() == null) {
            return createPost(postDTO);
        }

        // Cannot operate on other users' posts
        if (!(SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)
            || Objects.equals(postDTO.getUserId(), userService.getCurrentUserId()))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Post post = postMapper.postDTOToPost(postDTO);
        Post result = postService.updatePost(post);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("post", postDTO.getId().toString()))
            .body(postMapper.postToPostDTO(result));
    }

    /**
     * GET  /posts -> get all the posts.
     */
    @RequestMapping(value = "/posts",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostDTO>> getAllPosts(Pageable pageable, @RequestParam(defaultValue = "") String keywords)
        throws URISyntaxException {
        Page<Post> page;
        if (keywords.trim().equals("")) {
            page = postRepository.findUserPosts(userService.getCurrentUserId(), pageable);
        } else {
//            page = postRepository.searchUserPosts(pageable, userService.getCurrentUserId(), keywords.toUpperCase());
            page = postSearchRepository.searchUserPosts(keywords, SecurityUtils.getCurrentUserLogin(), pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/posts");
        return new ResponseEntity<>(page.getContent().stream()
            .map(postMapper::postToSimplePostDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /posts/tags/:id -> get all the posts filter by tag id
     */
    @RequestMapping(value = "/posts/tags/{tagId:\\d+}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostDTO>> getAllPostsByTag(Pageable pageable, @PathVariable Long tagId)
        throws URISyntaxException {

        Tag tag = tagRepository.findOne(tagId);

        if (tag == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Page<Post> page = postRepository.findUserPostsByTag(pageable, SecurityUtils.getCurrentUserLogin(), tagId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/posts/tags/" + tagId);

        List<Long> ids = page.getContent().stream()
            .map(Post::getId)
            .collect(Collectors.toList());

        List<Post> posts = postRepository.findByIdIn(ids);

        return new ResponseEntity<>(posts.stream()
            .map(postMapper::postToSimplePostDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * Get /posts/tags -> get all user tags with counting information
     */
    @RequestMapping(value = "/posts/tags",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<PostTagCountDTO>> getUserTags() {
        return new ResponseEntity<>(postService.countUserPostsByTags(userService.getCurrentUserId()), HttpStatus.OK);
    }

    /**
     * GET  /posts/:id -> get the "id" post.
     */
    @RequestMapping(value = "/posts/{id:\\d+}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id) {
        log.debug("REST request to get Post : {}", id);

        Post post = postRepository.findUserPost(id, userService.getCurrentUserId());

        // Enable for admin
        if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
            post = postRepository.findPostWithTags(id);
        }

        return Optional.ofNullable(post)
            .map(postMapper::postToPostDTO)
            .map(postDTO -> new ResponseEntity<>(postDTO, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST  /posts/:id -> get the "id" post.
     */
    @RequestMapping(value = "/posts/{id:\\d+}/vote",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PostVoteDTO> vote(@PathVariable Long id, @RequestParam(required = true) String type) {
        log.debug("REST request to vote-up Post : {}", id);

        Post post = postRepository.findByStatusAndId(PostStatus.PUBLIC, id);

        if (post == null || !(type.equals("up") || type.equals("down"))) {
            return new ResponseEntity<>(new PostVoteDTO(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        String result;
        if (type.equals("up")) {
            result = voteService.voteUp(post, userService.getUserWithAuthorities());
        } else {
            result = voteService.voteDown(post, userService.getUserWithAuthorities());
        }
        return new ResponseEntity<>(new PostVoteDTO(post, result), HttpStatus.OK);
    }

    /**
     * DELETE  /posts/:id -> delete the "id" post.
     */
    @RequestMapping(value = "/posts/{id:\\d+}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.debug("REST request to delete Post : {}", id);
        Post post = postRepository.findOne(id);

        // Cannot operate on other users' posts
        if (!Objects.equals(post.getUser().getId(), userService.getCurrentUserId())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        postService.deletePost(post);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("post", id.toString())).build();
    }
}
