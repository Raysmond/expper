package com.expper.service.search;

import com.expper.domain.Post;
import com.expper.repository.PostRepository;
import com.expper.repository.search.PostSearchRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class PostSearchService {

    @Autowired
    private PostSearchRepository postSearchRepository;

    @Autowired
    private PostRepository postRepository;

    // Deprecated
    @Async
    public void indexAll() {
        postRepository.findAll().forEach(postSearchRepository::save);
    }

    @Async
    public void index(Post post) {
        postSearchRepository.save(postRepository.findPostWithTags(post.getId()));
    }

    @Async
    public void deleteIndex(Long postId) {
        postSearchRepository.delete(postId);
    }

}
