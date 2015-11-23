package com.expper.service.util;

import com.expper.domain.Post;
import com.expper.domain.enumeration.PostStatus;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class RandomPost {

    public Post generateRandomPostFrom(Post post) {
        Post _post = new Post();
        _post.setUrl(post.getUrl());
        _post.setDomain(post.getDomain());
        _post.setUser(post.getUser());
        _post.setContent(post.getContent());
        _post.setStatus(PostStatus.PUBLIC);

        String title = "";

        for (int j = 0; j < 10; j++) {
            title += " " + RandomStringUtils.randomAlphabetic((int) (Math.random() * 10) + 1);
        }
        _post.setTitle(title);

        return _post;
    }
}
