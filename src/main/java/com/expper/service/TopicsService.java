package com.expper.service;

import com.expper.domain.Topic;
import com.expper.repository.TagRepository;
import com.expper.repository.TopicRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class TopicsService {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TopicRepository topicRepository;

    public static final String CACHE_TOPICS = "cache.topics";

    @Cacheable(value = CACHE_TOPICS, key = "'topics'")
    public List<Topic> getAll() {
        return topicRepository.findAllWithEagerRelationships();
    }

    @Cacheable(value = CACHE_TOPICS, key = "'topic_'+#name")
    public Topic getTopicByName(String name) {
        return topicRepository.findOneWithEagerRelationships(name);
    }

    @CacheEvict(value = CACHE_TOPICS, key = "'topics'")
    public Topic createTopic(Topic topic) {
        return topicRepository.save(topic);
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_TOPICS, key = "'topics'"),
        @CacheEvict(value = CACHE_TOPICS, key = "'topic_'+#topic.name")
    })
    public Topic updateTopic(Topic topic) {
        return topicRepository.save(topic);
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_TOPICS, key = "'topics'"),
        @CacheEvict(value = CACHE_TOPICS, key = "'topic_'+#topic.name")
    })
    public void deleteTopic(Topic topic) {
        topicRepository.delete(topic);
    }

}
