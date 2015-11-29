package com.expper.service;

import com.expper.domain.Message;
import com.expper.domain.Post;
import com.expper.domain.Reply;
import com.expper.domain.User;
import com.expper.domain.enumeration.MessageType;
import com.expper.domain.enumeration.PostStatus;
import com.expper.repository.PostRepository;
import com.expper.repository.ReplyRepository;
import com.expper.repository.UserRepository;
import com.expper.service.support.EmailParams;
import com.expper.web.rest.dto.PostDTO;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class RabbitConsumer {
    private final Logger log = LoggerFactory.getLogger(RabbitConsumer.class);

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private HotPostService hotPostService;

    @Autowired
    private MailService mailService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private UserRepository userRepository;

    public void handleMessage(PostDTO postDTO) {
        log.debug("Processing saving new post, url=" + postDTO.getUrl());

        try {
            postService.createPost(postDTO);
        } catch (Exception e) {
            log.error("Failed to save web post, url=" + postDTO.getUrl() + ", with errors: " + e.getMessage());
        }
    }

    /**
     * Handle adding public post
     */
    public void handleMessage(Post post) {
        log.debug("Handle new public post: " + post.toString());

    }

    /**
     * Get full text of a post
     */
    public void getArticle(Post post) {
        log.debug("Handle crawling article full text from source site, id=" + post.getId() + " , url=" + post.getUrl());

        try {
            String result = postService.getWebPost(String.format(PostService.SERVER_URL, post.getUrl()));
            if (result == null) {
                log.error("Failed to get article full text, id=" + post.getId());
                return;
            }

            Post resultPost = postRepository.findOne(post.getId());
            if (resultPost == null) {
                log.warn("Cancel crawling article full text of post id=" + post.getId() + ", because the post does not exist.");
                return;
            }

            JSONObject json = new JSONObject(result);
            String content = json.getString("content");
            content = Jsoup.clean(content, Whitelist.relaxed());
            resultPost.setContent(content);

            postService.saveNewPost(resultPost);
        } catch (Exception e) {
            log.error("Failed to resolve article full text, id=" + post.getId() + ", url=" + post.getUrl() + ", exception: " + e.getMessage());
        }
    }


    /**
     * Queue for updating post score and updating hot post list
     */
    public void updatePostScore(Post post) {
        log.debug("Handle updating post score, id=" + post.getId());

        Post postToUpdate = postService.getPost(post.getId());

        if (postToUpdate == null) {
            log.warn("Post id=" + post.getId() + " does not exist now.");
            return;
        }

        if (postToUpdate.getStatus() == PostStatus.PRIVATE) {
            log.warn("Post id=" + post.getId() + " is private, no need to update score.");
            return;
        }

        hotPostService.addHotPost(postToUpdate);
        hotPostService.addTaggedPost(postToUpdate, postToUpdate.getTags());
    }

    /**
     * Queue to send emails
     */
    public void sendEmail(EmailParams emailParams) throws IOException {
        log.debug("Handle sending email.");

        mailService.doSendEmail(emailParams);
    }

    /**
     * Handling messages/notifications
     */
    public void addMessage(Message message) {
        log.debug("Handle adding message.");

        Post post;
        switch (message.getType()) {
            case NEW_REPLY:
                post = postRepository.findOne(message.getPost().getId());
                message.setToUser(post.getUser());

                if (!message.getToUser().getId().equals(message.getByUser().getId())){
                    messageService.createMessage(message);
                }

                Matcher atUsers = Pattern.compile("\\@[0-9a-zA-Z]+").matcher(message.getContent());
                while (atUsers.find()) {
                    String name = atUsers.group().substring(1);

                    User user = userRepository.findByLogin(name);

                    if (user != null) {
                        messageService.createMessage(
                            new Message(
                                user, message.getByUser(),
                                post.getTitle(), message.getContent(),
                                post,
                                MessageType.NEW_REPLY_TO_REPLY));
                    }
                }


                break;

            case NEW_VOTE:
                messageService.createMessage(message);
                break;
            default:
                log.warn("Unrecognized message: " + message.toString());
        }

    }
}
