package com.expper.service;

import com.expper.config.JHipsterProperties;
import com.expper.config.QiniuConfig;
import com.expper.domain.User;
import com.expper.repository.UserRepository;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class QiniuService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JHipsterProperties jHipsterProperties;

    @Autowired
    private Auth qiniuAuth;

    private UploadManager uploadManager = new UploadManager();

    public String pictureKey(Long userId) {
        return "pictures/pic_" + UUID.randomUUID();
    }

    public String authToken(String key) {
        return qiniuAuth.uploadToken(jHipsterProperties.getQiniu().getBucket(), key);
    }

    public QiniuResult uploadPicture(String login, MultipartFile picture) throws IOException {
        User user = userRepository.findByLogin(login);
        String key = pictureKey(user.getId());
        String token = authToken(key);

        File file = convert(picture);
        Response res = uploadManager.put(file, key, token, null, null, true);
        QiniuResult result = res.jsonToObject(QiniuResult.class);

        file.delete();

        user.setPicture(key);
        userRepository.save(user);

        return result;
    }

    public File convert(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public static class QiniuResult {
        public String hash;
        public String key;
        public String fsize;
        public String fname;
        public String mimeType;

        @Override
        public String toString() {
            return "Result{" +
                "hash='" + hash + '\'' +
                ", key='" + key + '\'' +
                ", fsize='" + fsize + '\'' +
                ", fname='" + fname + '\'' +
                ", mimeType='" + mimeType + '\'' +
                '}';
        }
    }
}
