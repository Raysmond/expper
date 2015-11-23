package com.expper.service;

import com.google.gson.JsonObject;

import com.expper.config.RabbitmqConfiguration;
import com.expper.domain.Message;
import com.expper.domain.Post;
import com.expper.domain.Reply;
import com.expper.domain.Vote;
import com.expper.domain.enumeration.MessageType;
import com.expper.repository.MessageRepository;
import com.expper.repository.PostRepository;
import com.fasterxml.jackson.databind.util.JSONPObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class MessageService {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    PostRepository postRepository;

    @Resource(name = "redisCountingTemplate")
    HashOperations<String, String, Integer> counting;

    public static final String MESSAGE_QUEUE = RabbitmqConfiguration.QUEUE_ADD_MESSAGE;

    public static final String CACHE_COUNT_USER_MESSAGES = "counting.user.messages";

    /**
     * Create message and save it to DB
     */
    public void createMessage(Message message) {
        messageRepository.save(message);

        if (message.getToUser() != null) {
            incUserUnreadMessages(message.getToUser().getId());
        }
    }

    public Integer getUserUnreadMessagesCount(Long userId) {
        Integer val = counting.get(CACHE_COUNT_USER_MESSAGES, userId.toString());

        if (val == null) {
            val = (int) messageRepository.countUserUnreadMessages(userId);
            counting.put(CACHE_COUNT_USER_MESSAGES, userId.toString(), val);
        }

        return val;
    }

    public void incUserUnreadMessages(Long userId) {
        counting.increment(CACHE_COUNT_USER_MESSAGES, userId.toString(), 1);
    }

    public void decUserUnreadMessages(Long userId) {
        counting.increment(CACHE_COUNT_USER_MESSAGES, userId.toString(), -1);
    }

    @Async
    public void emptyUserUnreadMessages(Long userId) {
        if (getUserUnreadMessagesCount(userId) > 0) {
            messageRepository.readUserMessages(userId);
        }

        counting.put(CACHE_COUNT_USER_MESSAGES, userId.toString(), 0);
    }

    // END OF COUNTING ----------------------------------------------


    // BEGIN OF MESSAGE SERVICES -----------------------------------

    public void newReplyMessage(Reply reply) {
        // message.toUser is not set yet, this will load lazily in RabbitMQ handling method
        Message message = new Message(
            null, reply.getUser(),
            null, reply.getContent(),
            reply.getPost(), MessageType.NEW_REPLY);

        message.addParam("reply_id", reply.getId());

        rabbitTemplate.convertAndSend(MESSAGE_QUEUE, message);
    }

    public void newVoteMessage(Vote vote, Post post, String result) {
        Message message = new Message(
            post.getUser(), vote.getUser(),
            post.getTitle(), vote.getType().toString(),
            vote.getPost(), MessageType.NEW_VOTE);

        message.addParam("vote_id", vote.getId());
        message.addParam("vote_type", vote.getType().toString());
        message.addParam("vote_result_type", result);

        rabbitTemplate.convertAndSend(MESSAGE_QUEUE, message);
    }

}
