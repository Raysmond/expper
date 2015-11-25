package com.expper.service;

import com.codahale.metrics.annotation.Timed;
import com.expper.config.RabbitmqConfiguration;
import com.expper.domain.Post;
import com.expper.domain.User;
import com.expper.domain.Vote;
import com.expper.domain.enumeration.VoteType;
import com.expper.repository.PostRepository;
import com.expper.repository.TagRepository;
import com.expper.repository.VoteRepository;
import com.expper.security.SecurityUtils;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.Resource;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class VoteService {
    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private CountingService countingService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserService userService;

    // cache ?
    public Vote getVote(Post post, User user) {
        return voteRepository.findByUserAndPost(user, post);
    }

    public static final String VOTE_UP = "VOTE_UP";
    public static final String VOTE_UP_FROM_DOWN = "VOTE_UP_FROM_DOWN";
    public static final String VOTE_DOWN = "VOTE_DOWN";
    public static final String VOTE_DOWN_FROM_UP = "VOTE_DOWN_FROM_UP";
    public static final String CANCEL_VOTE_UP = "CANCEL_VOTE_UP";
    public static final String CANCEL_VOTE_DOWN = "CANCEL_VOTE_DOWN";

    /**
     * Vote up for a post
     *
     * @return true if up vote is created, false if up vote is deleted
     */
    @Transactional
    @Timed
    @CacheEvict(value = "cache.user_vote", key = "'vote_'+#postId.toString() + '_' + #userId.toString()")
    public String voteUp(Post post, User user) {
        Vote vote = getVote(post, user);

        String result = VOTE_UP;
        if (vote == null || vote.getType() == VoteType.DOWN) {
            if (vote != null) {
                result = VOTE_UP_FROM_DOWN;
                voteRepository.delete(vote);
                //post.cancelVoteDown();
                voteRepository.flush();

                countingService.decVoteDown(post.getId());
            }

            vote = new Vote(post, user, VoteType.UP);
            voteRepository.save(vote);
            //post.voteUp();

            countingService.incVoteUp(post.getId());
        } else {
            result = CANCEL_VOTE_UP;
            voteRepository.delete(vote);
            //post.cancelVoteUp();

            countingService.decVoteUp(post.getId());
        }

        afterVote(vote, post, result);

        return result;
    }

    @Transactional
    @Timed
    @CacheEvict(value = "cache.user_vote", key = "'vote_'+#postId.toString() + '_' + #userId.toString()")
    public String voteDown(Post post, User user) {
        Vote vote = getVote(post, user);

        String result = VOTE_DOWN;

        if (vote == null || vote.getType() == VoteType.UP) {
            if (vote != null) {
                result = VOTE_DOWN_FROM_UP;
                voteRepository.delete(vote);
                voteRepository.flush();
                //post.cancelVoteUp();

                countingService.decVoteUp(post.getId());
            }

            vote = new Vote(post, user, VoteType.DOWN);
            voteRepository.save(vote);
            //post.voteDown();

            countingService.incVoteDown(post.getId());
        } else {
            result = CANCEL_VOTE_DOWN;
            voteRepository.delete(vote);
            //post.cancelVoteDown();

            countingService.decVoteDown(post.getId());
        }

        afterVote(vote, post, result);

        return result;
    }

    /**
     * Callback after creating a new vote
     */
    private void afterVote(Vote vote, Post post, String result) {
        // Update to hot posts
        post.setVotesUp(countingService.getVoteUp(post.getId()));
        post.setVotesDown(countingService.getVoteDown(post.getId()));

        // Update post score and popular post lists
        rabbitTemplate.convertAndSend(RabbitmqConfiguration.QUEUE_UPDATE_POST_SCORE, post);

        // Send notification message
        messageService.newVoteMessage(vote, post, result);
    }

    @Cacheable(value = "cache.user_vote", key = "'vote_'+#postId.toString() + '_' + #userId.toString()")
    public Vote checkUserVote(Long postId, Long userId) {
        Post post = new Post();
        post.setId(postId);
        User user = new User();
        user.setId(userId);
        return voteRepository.findByUserAndPost(user, post);
    }

    // TODO
    @Timed
    public List<Vote> checkUserVotes(List<Post> posts, Long userId) {
        Set<Long> ids = new HashSet<>();
        posts.forEach(post -> ids.add(post.getId()));
        return voteRepository.getUserVotes(userId, ids);
    }

    public Map<Long, Vote> getCurrentUserVoteMapFor(List<Post> posts) {
        if (SecurityUtils.isAuthenticated()) {
            return getUserVoteMapFor(posts, userService.getCurrentUserId());
        } else {
            return new HashMap<>();
        }
    }

    public Map<Long, Vote> getUserVoteMapFor(List<Post> posts, Long userId) {
        Map<Long, Vote> votesMap = new HashMap<>();
        if (posts == null || posts.isEmpty()) {
            return votesMap;
        }

        List<Vote> votes = this.checkUserVotes(posts, userId);
        votes.forEach(vote -> votesMap.put(vote.getPostId(), vote));
        return votesMap;
    }

    @Timed
    public Map<Long, Vote> getUserVoteMap(Collection<Long> postIds, Long userId) {
        Map<Long, Vote> votesMap = new HashMap<>();
        if (postIds == null || postIds.isEmpty()) {
            return votesMap;
        }

        Set<Long> ids = new HashSet<>();
        postIds.forEach(ids::add);
        List<Vote> votes = voteRepository.getUserVotes(userId, ids);

        votes.forEach(vote -> votesMap.put(vote.getPostId(), vote));
        return votesMap;
    }

}
