package com.expper.service;

import com.expper.domain.Post;
import com.expper.domain.Tag;
import com.expper.domain.User;
import com.expper.repository.TagRepository;
import com.expper.repository.UserRepository;
import com.expper.service.util.StringUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CountingService countingService;

    public static final String CACHE_TAG = "entity.tag";
    public static final String CACHE_COUNT_USER = "cache.count.user";

    public Tag findByName(String name) {
        return tagRepository.findByName(name);
    }

    public Tag findOrCreateByName(Post post, String friendlyName) {
        String name = StringUtil.toMachineString(friendlyName);
        Tag tag = tagRepository.findByName(name);
        if (tag == null) {
            tag = tagRepository.save(new Tag(post.getUser(), name, friendlyName));
            countingService.incTagsCount();
        }
        return tag;
    }

    public void deleteTag(Tag tag){
        tagRepository.delete(tag);
        countingService.decTagsCount();
    }

    // TODO
    public synchronized void increasePostCountByOne(Tag tag) {
        tag.increasePostCountByOne();
        tagRepository.save(tag);
    }

    // TODO
    public synchronized void decreasePostCountByOne(Tag tag) {
        tag.decreasePostCountByOne();
        tagRepository.save(tag);
    }

    // TODO
    public Page<Tag> getHotTags(int page, int size) {
        return tagRepository.findAll(new PageRequest(page, size, Sort.Direction.DESC, "postCount", "followersCount"));
    }

    // TODO
    public Long countTags() {
        return tagRepository.count();
    }

    @Cacheable(value=CACHE_COUNT_USER, key="#userId.toString().concat('_followed_tags_count')")
    public Long countUserFollowedTags(Long userId) {
        return tagRepository.countUserFollowedTags(userId);
    }

    public List<Tag> getUserTags(int page, int size, Long userId) {
        return tagRepository.findUserTags(userId, size, page * size);
    }

    // TODO
    @Transactional(readOnly = true)
    public Map<Long, Boolean> isUserFollowedTags(List<Long> tagIds, Long userId) {
        Map<Long, Boolean> res = new HashMap<>();

        User user = userRepository.findOne(userId);
        user.getFollowingTags().size();

        tagIds.forEach(id -> {
            boolean followed = false;
            for (Tag tag : user.getFollowingTags()) {
                if (Objects.equals(tag.getId(), id)) {
                    followed = true;
                    break;
                }
            }
            res.put(id, followed);
        });

        return res;
    }


    @Transactional
    @CacheEvict(value=CACHE_COUNT_USER, key="#userId.toString().concat('_followed_tags_count')")
    public synchronized Long followTag(Long userId, Tag tag) {
        User user = userRepository.findOne(userId);

        if (user.getFollowingTags().contains(tag))
            return tag.getFollowersCount();

        user.getFollowingTags().add(tag);
        userRepository.save(user);

        tag.setFollowersCount(tag.getFollowersCount() + 1);
        tagRepository.save(tag);

        return tag.getFollowersCount();
    }

    @Transactional
    @CacheEvict(value=CACHE_COUNT_USER, key="#userId.toString().concat('_followed_tags_count')")
    public synchronized Long unFollowTag(Long userId, Tag tag) {
        User user = userRepository.findOne(userId);

        if (!user.getFollowingTags().contains(tag))
            return tag.getFollowersCount();

        user.getFollowingTags().remove(tag);
        userRepository.save(user);

        tag.setFollowersCount(tag.getFollowersCount() - 1);
        tagRepository.save(tag);

        return tag.getFollowersCount();
    }
}
