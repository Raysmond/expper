package com.expper.web;

import com.expper.domain.Message;
import com.expper.repository.MessageRepository;
import com.expper.security.SecurityUtils;
import com.expper.service.MessageService;
import com.expper.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    public static final int PAGE_SIZE = 20;

    @RequestMapping(value = "/me/messages", method = RequestMethod.GET)
    public String showUserMessages(Model model, @RequestParam(defaultValue = "1") int page) {
        page = page > 1 ? page - 1 : 0;

        Long userId = userService.getCurrentUserId();

        Page<Message> messages = messageRepository.findUserMessages(
            userId,
            new PageRequest(page, PAGE_SIZE, Sort.Direction.DESC, "id"));

        model.addAttribute("page", page + 1);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("messages", messages);

        messageService.emptyUserUnreadMessages(userId);

        return "messages/user_all";
    }
}
