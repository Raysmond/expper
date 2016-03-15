package com.expper.service.util;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class StringUtil {

    public static String toMachineString(String str) {
        String token = str.toLowerCase().replace("\n", " ").replaceAll("[\\s]", " ").replaceAll("/", " ");
        return StringUtils.arrayToDelimitedString(StringUtils.tokenizeToStringArray(token, " "), "-");
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.getHost();
    }
}
