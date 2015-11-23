package com.expper.repository;

import com.expper.domain.Post;
import com.expper.domain.User;
import com.expper.domain.Vote;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Raysmond<i@raysmond.com>
 */
public interface VoteRepository extends JpaRepository<Vote, Long>{
    Vote findByUserAndPost(User user, Post post);

    @Query("select v from Vote v where v.user.id = :uid and v.post.id in :ids")
    List<Vote> getUserVotes(@Param("uid") Long userId, @Param("ids") Set<Long> ids);
}
