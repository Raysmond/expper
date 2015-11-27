package com.expper.repository.search;

import com.expper.domain.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Raysmond<i@raysmond.com>
 */
public interface PostSearchRepository extends ElasticsearchRepository<Post, Long> {

    Page<Post> findByTitleLike(String title, Pageable pageable);

}
