package com.expper.service;

import com.expper.config.JHipsterProperties;
import com.expper.config.RabbitmqConfiguration;
import com.expper.domain.User;
import com.expper.service.support.BasicAuthRestTemplate;
import com.expper.service.support.EmailParams;

import org.apache.commons.lang.CharEncoding;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;


import javax.inject.Inject;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

/**
 * Service for sending e-mails. <p/> <p> We use the @Async annotation to send e-mails
 * asynchronously. </p>
 */
@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    @Inject
    private JHipsterProperties jHipsterProperties;

    @Inject
    private JavaMailSenderImpl javaMailSender;

    @Inject
    private MessageSource messageSource;

    @Inject
    private SpringTemplateEngine templateEngine;

    @Inject
    private RabbitTemplate rabbitTemplate;

    private static final String MAILGUN_USER_NAME = "api";

    /**
     * System default email address that sends the e-mails.
     */
    private String from;

    /**
     * Invoked from RabbitMQ message handler
     */
    public void doSendEmail(EmailParams emailParams) throws IOException {
        List<String> filterDomains = new ArrayList<>();
        filterDomains.add("qq.com");
        filterDomains.add("foxmail.com");
        filterDomains.add("fudan.edu.cn");
        filterDomains.add("hotmail.com");
        filterDomains.add("outlook.com");

        /*
        String to = emailParams.getTo();
        boolean inFilterDomain = false;
        for (String domain : filterDomains){
            if (to.endsWith(domain)){
                inFilterDomain = true;
                break;
            }
        }

        if (inFilterDomain){
            sendViaSendcloudApi(emailParams);
        } else {
            sendViaMailgunApi(emailParams);
        }
        */

        sendViaSendcloudApi(emailParams);
    }

    public void sendViaMailgunApi(EmailParams emailParams) {
        String apiUrl = jHipsterProperties.getMailgun().getApiUrl();
        String apiKey = jHipsterProperties.getMailgun().getApiKey();

        MultiValueMap<String, Object> vars = new LinkedMultiValueMap<>();
        vars.add("from", jHipsterProperties.getMailgun().getFrom());
        vars.add("to", emailParams.getTo());
        vars.add("subject", emailParams.getSubject());
        vars.add("html", emailParams.getContent());

        RestTemplate restTemplate = new BasicAuthRestTemplate(MAILGUN_USER_NAME, apiKey);
        restTemplate.postForLocation(apiUrl, vars);

        log.debug("Email sent successfully.");
    }

    public void sendViaSendcloudApi(EmailParams emailParams) throws IOException {
        String url = jHipsterProperties.getSendcloud().getUrl();
        String key = jHipsterProperties.getSendcloud().getKey();
        String user = jHipsterProperties.getSendcloud().getUser();
        String from = jHipsterProperties.getSendcloud().getFrom();
        String fromName = jHipsterProperties.getSendcloud().getFromName();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("api_user", user));
        params.add(new BasicNameValuePair("api_key", key));
        params.add(new BasicNameValuePair("to", emailParams.getTo()));
        params.add(new BasicNameValuePair("from", from));
        params.add(new BasicNameValuePair("fromname", fromName));
        params.add(new BasicNameValuePair("subject", emailParams.getSubject()));
        params.add(new BasicNameValuePair("html", emailParams.getContent()));
        params.add(new BasicNameValuePair("resp_email_id", "true"));

        HttpPost httpost = new HttpPost(url);
        HttpClient httpclient = new DefaultHttpClient();

        httpost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpclient.execute(httpost);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            log.debug("Send email via Sendcloud successfully. response: " + EntityUtils.toString(response.getEntity()));
        } else {
            log.error("Send email via Sendcloud failed.");
        }

        httpost.releaseConnection();
    }

    /**
     * Send emails via RabbitMQ queue
     */
    private void sendMailToQueue(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        EmailParams emailParams = new EmailParams(to, subject, content, isMultipart, isHtml);
        rabbitTemplate.convertAndSend(RabbitmqConfiguration.QUEUE_SEND_EMAIL, emailParams);
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug("Send e-mail[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, CharEncoding.UTF_8);
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMailgun().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.debug("Sent e-mail to User '{}'", to);
        } catch (Exception e) {
            log.warn("E-mail could not be sent to user '{}', exception is: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendActivationEmail(User user, String baseUrl) {
        log.debug("Sending activation e-mail to '{}'", user.getEmail());
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable("user", user);
        context.setVariable("baseUrl", baseUrl);
        String content = templateEngine.process("activationEmail", context);
        String subject = messageSource.getMessage("email.activation.title", null, locale);
//        sendEmail(user.getEmail(), subject, content, false, true);
        sendMailToQueue(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendPasswordResetMail(User user, String baseUrl) {
        log.debug("Sending password reset e-mail to '{}'", user.getEmail());
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable("user", user);
        context.setVariable("baseUrl", baseUrl);
        String content = templateEngine.process("passwordResetEmail", context);
        String subject = messageSource.getMessage("email.reset.title", null, locale);
//        sendEmail(user.getEmail(), subject, content, false, true);
        sendMailToQueue(user.getEmail(), subject, content, false, true);
    }

}
