package com.expper.repository;

import com.expper.domain.Topic;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Topic entity.
 */
public interface TopicRepository extends JpaRepository<Topic,Long> {

    @Query("select distinct topic from Topic topic left join fetch topic.tags order by topic.weight desc")
    List<Topic> findAllWithEagerRelationships();

    @Query("select topic from Topic topic left join fetch topic.tags where topic.id =:id")
    Topic findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select topic from Topic topic left join fetch topic.tags where topic.name =:name")
    Topic findOneWithEagerRelationships(@Param("name") String name);
}
