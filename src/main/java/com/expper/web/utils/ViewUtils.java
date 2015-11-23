package com.expper.web.utils;

import com.expper.config.QiniuConfig;
import com.expper.domain.Message;
import com.expper.domain.User;
import com.expper.domain.enumeration.MessageType;
import com.expper.domain.enumeration.VoteType;
import com.expper.repository.UserRepository;
import com.expper.security.SecurityUtils;
import com.expper.service.MessageService;
import com.expper.service.UserService;
import com.expper.service.TagService;
import com.expper.service.PostService;
import com.qiniu.common.Zone;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class ViewUtils {
    @Inject
    UserService userService;

    @Inject
    PostService postService;

    @Inject
    TagService tagService;

    @Inject
    MessageService messageService;

    @Inject
    UserRepository userRepository;

    private static final PrettyTime TIME = new PrettyTime(Locale.SIMPLIFIED_CHINESE);

    public String timeAgo(ZonedDateTime dateTime) {
        return TIME.format(Date.from(dateTime.toInstant()));
    }

    public String timeAgo(DateTime dateTime) {
        return TIME.format(dateTime.toDate());
    }

    public String timeAgo(Date date) {
        return TIME.format(date);
    }

    public static String timeAgoOf(ZonedDateTime dateTime) {
        return TIME.format(Date.from(dateTime.toInstant()));
    }

    public static String pictureUrl(String picture) {
        if (picture == null)
            return null;

        return QiniuConfig.domain + "/" + picture + "-64w";
    }

    public User getCurrentUser() {
        return userRepository.findByLogin(SecurityUtils.getCurrentUserLogin());
    }

    public int getCurrentUserUnreadMessagesCount() {
        return messageService.getUserUnreadMessagesCount(userService.getCurrentUserId());
    }

    public String getUserPicture(User user) {
        String pic = user.getPicture();
        if (pic == null || pic.isEmpty()) {
            return null;
        }

        return pictureUrl(pic);
    }

    public Long getUserPostCount(User user) {
        return postService.getUserPostsCount(user.getId());
    }

    public Long getUserFollowedTagsCount(User user) {
        return tagService.countUserFollowedTags(user.getId());
    }

    public String getFormatDate(ZonedDateTime dateTime) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return dateTime.format(format);
    }

    public String getVoteMessageType(Message message) {
        String type = "";
        try {
            JSONObject params = message.getParamsJson();
            if (params != null && params.has("vote_result_type")) {
                return params.getString("vote_result_type");
            }
        } catch (JSONException ignored) {
        }

        return type;
    }
}
