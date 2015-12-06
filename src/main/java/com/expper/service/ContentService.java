package com.expper.service;

import com.expper.domain.Content;
import com.expper.domain.enumeration.ContentStatus;
import com.expper.repository.ContentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class ContentService {
    @Autowired
    private ContentRepository contentRepository;

    public static final String CACHE_CONTENT = "cache.content";

    @Cacheable(value = CACHE_CONTENT, key = "#id.toString()")
    public Content getPublishedContent(Long id) {
        return contentRepository.findOneByIdAndStatus(id, ContentStatus.PUBLISHED);
    }

    @Cacheable(value = CACHE_CONTENT, key = "'permalink_'+#permaklink")
    public Content getPublishedContent(String permaklink) {
        return contentRepository.findOneByPermalink(permaklink);
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_CONTENT, key = "#content.id.toString()"),
        @CacheEvict(value = CACHE_CONTENT, key =  "'permalink_'+#content.permalink")
    })
    public Content updateContent(Content content) {
        return contentRepository.save(content);
    }

}
