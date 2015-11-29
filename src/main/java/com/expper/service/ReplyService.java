package com.expper.service;

import com.expper.config.RabbitmqConfiguration;
import com.expper.domain.Reply;
import com.expper.domain.User;
import com.expper.domain.enumeration.PostStatus;
import com.expper.domain.enumeration.ReplyStatus;
import com.expper.repository.ReplyRepository;
import com.expper.repository.UserRepository;
import com.expper.security.SecurityUtils;
import com.expper.web.rest.dto.ReplyDTO;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class ReplyService {
    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private CountingService countingService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MessageService messageService;

    private String updateAtUser(String content) {
        Matcher atUsers = Pattern.compile("\\@[0-9a-zA-Z]+").matcher(content);

        Set<String> names = new HashSet<>();
        while (atUsers.find()) {
            names.add(atUsers.group().substring(1));
        }

        for (String name : names) {
            content = content.replace("@" + name + " ", "<a href=\"/u/" + name + "\">@" + name + "</a>&nbsp;");
        }

        return content;
    }

    public Reply createReply(ReplyDTO replyDTO, User user) {
        replyDTO.setUserId(user.getId());
        Reply reply = replyDTO.toReply();

        String content = Jsoup.clean(reply.getContent(), Whitelist.basicWithImages());
        content = updateAtUser(content);

        reply.setContent(content);
        reply.setStatus(ReplyStatus.ACTIVE);

        Reply result = replyRepository.save(reply);

        reply.setUser(user);

        afterCreatingReply(reply);
        return result;
    }

    private void afterCreatingReply(Reply reply) {
        countingService.incReplies(reply.getPost().getId());
        rabbitTemplate.convertAndSend(RabbitmqConfiguration.QUEUE_UPDATE_POST_SCORE, reply.getPost());
        messageService.newReplyMessage(reply);
    }

    public void deleteReply(Long id) {
        Reply reply = replyRepository.getOne(id);
        Long postId = reply.getPost().getId();
        replyRepository.delete(id);
        countingService.decReplies(postId);

        rabbitTemplate.convertAndSend(RabbitmqConfiguration.QUEUE_UPDATE_POST_SCORE, reply.getPost());
    }
}
