package com.expper.repository;

import com.expper.domain.Reply;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Reply entity.
 */
public interface ReplyRepository extends JpaRepository<Reply,Long> {

    @Query("select reply from Reply reply where reply.user.login = ?#{principal.username}")
    List<Reply> findByUserIsCurrentUser();

    @Query("select reply from Reply reply where reply.post.id = :postId")
    Page<Reply> findAllByPost(Pageable pageable, @Param("postId") Long postId);

}
