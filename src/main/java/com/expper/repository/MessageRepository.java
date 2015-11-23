package com.expper.repository;

import com.expper.domain.Message;
import com.expper.domain.enumeration.MessageStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Raysmond<i@raysmond.com>
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m where m.toUser.id = ?1 and m.status = ?2")
    Page<Message> findUserUnreadMessages(Long userId, MessageStatus status, Pageable pageable);

    @Query("select m from Message m where m.toUser.id = :uid")
    Page<Message> findUserMessages(@Param("uid") Long userId, Pageable pageable);

    @Query(value = "select count(m) from message m where m.to_user_id = ?1 and m.status = 'UNREAD'", nativeQuery = true)
    long countUserUnreadMessages(Long userId);

    @Modifying
    @Transactional
    @Query(value = "update message set status='READ' where status='UNREAD' and to_user_id = ?1", nativeQuery = true)
    void readUserMessages(Long toUserId);
}
