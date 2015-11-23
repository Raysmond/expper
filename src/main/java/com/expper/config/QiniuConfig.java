package com.expper.config;

import com.qiniu.util.Auth;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class QiniuConfig {
    public static final Auth qiniuAuth = Auth.create(
        "gknq31BHuKq5GR0rGVvn-3rEl3ununZSszWkjU9F",
        "BYdPoC8VnNvbev98gV1BjZyWsY19KCkdZaxCxcaZ");

    public static final String bucket = "expper";
    public static final String domain = "https://dn-expper.qbox.me";
}
