package com.expper.repository.search;

import com.expper.domain.Post;
import com.expper.domain.enumeration.PostStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Raysmond<i@raysmond.com>
 */
public interface PostSearchRepository extends ElasticsearchRepository<Post, Long> {

    Page<Post> findByTitleLikeAndStatus(String title, PostStatus postStatus, Pageable pageable);

    @Query("{" +
        "        \"bool\": {" +
        "            \"must\": [" +
        "                {" +
        "                    \"term\": {" +
        "                        \"user.login\": \"?1\"" +
        "                    }" +
        "                }, " +
        "                {" +
        "                    \"bool\": {" +
        "                        \"should\": [" +
        "                            {" +
        "                                \"match\": {" +
        "                                    \"title\": \"?0\"" +
        "                                }" +
        "                            }, " +
        "                            {" +
        "                                \"match\": {" +
        "                                    \"tags.friendly_name\": \"?0\"" +
        "                                }" +
        "                            }" +
        "                        ]" +
        "                    }" +
        "                }" +
        "            ]" +
        "        }" +
        "    }")
    Page<Post> searchUserPosts(String query, String login, Pageable pageable);

}
