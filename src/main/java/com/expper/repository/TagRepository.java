package com.expper.repository;

import com.expper.domain.Tag;
import com.expper.service.TagService;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

/**
 * @author Raysmond<i@raysmond.com>
 */
public interface TagRepository extends JpaRepository<Tag, Long> {
    @Cacheable(TagService.CACHE_TAG)
    Tag findByName(String name);

    @Override
    @CacheEvict(value = TagService.CACHE_TAG, key = "#p0.name")
    void delete(Tag entity);

    @Override
    @CacheEvict(value = TagService.CACHE_TAG, key = "#p0.name")
    <S extends Tag> S save(S entity);

    @Query("SELECT post.tags FROM Post post WHERE post.id = ?1")
    Set<Tag> findPostTags(Long postId);

    @Query("SELECT tag FROM Tag tag WHERE UPPER(tag.friendlyName) LIKE %?1%")
    Page<Tag> searchTags(String name, Pageable pageable);

    @Query(value = "SELECT COUNT(tag.id) FROM tags tag JOIN follow_tags ft " +
        "ON ft.tag_id = tag.id WHERE ft.user_id = ?1", nativeQuery = true)
    Long countUserFollowedTags(Long userId);

    // TODO what's the simple way to do this page query?
    @Query(value =
        "SELECT tag.* FROM tags tag " +
        "JOIN follow_tags ft ON ft.tag_id = tag.id " +
        "WHERE ft.user_id = ?1 " +
        "ORDER BY tag.post_count DESC " +
        "LIMIT ?2 OFFSET ?3",
        nativeQuery = true)
    List<Tag> findUserTags(Long userId, int limit, int offset);

    @Query(value = "SELECT tag.* FROM tags tag JOIN follow_tags ft ON ft.tag_id = tag.id " +
        "WHERE ft.user_id = ?1 AND tag.id = ?2 LIMIT 1", nativeQuery = true)
    Tag getUserFollowedTag(Long userId, Long tagId);
}
