package com.expper.service.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class EmailParams implements Serializable{
    private String to;
    private String subject;
    private String content;
    private boolean isMultipart;
    private boolean isHtml;

    public EmailParams() {
    }

    public EmailParams(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.isMultipart = isMultipart;
        this.isHtml = isHtml;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isMultipart() {
        return isMultipart;
    }

    public void setIsMultipart(boolean isMultipart) {
        this.isMultipart = isMultipart;
    }

    public boolean isHtml() {
        return isHtml;
    }

    public void setIsHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }
}
