package com.expper.repository;

import com.expper.domain.Content;
import com.expper.domain.enumeration.ContentStatus;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Content entity.
 */
public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query("select content from Content content where content.user.login = ?#{principal.username}")
    List<Content> findByUserIsCurrentUser();

    Content findOneByIdAndStatus(Long id, ContentStatus contentStatus);

    Content findOneByPermalink(String permalink);
}
