package com.expper.service;

import com.expper.config.QiniuConfig;
import com.expper.domain.User;
import com.expper.repository.UserRepository;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Service
public class QiniuService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private UploadManager uploadManager = new UploadManager();

    public String pictureKey(Long userId) {
        return "pictures/pic_" + userId;
    }

    public String pictureToken(Long userId) {
        return QiniuConfig.qiniuAuth.uploadToken(QiniuConfig.bucket, pictureKey(userId));
    }

    public Result uploadPicture(String login, MultipartFile picture) throws IOException {
        User user = userRepository.findByLogin(login);
        Long userId = user.getId();

        String token = pictureToken(userId);

        File file = convert(picture);
        Response res = uploadManager.put(file, pictureKey(userId), token, null, null, true);
        Result result = res.jsonToObject(Result.class);

        file.delete();

        user.setPicture(pictureKey(userId));
        userRepository.save(user);

        return result;
    }

    public File multipartToFile(MultipartFile multipart) throws IOException {
        File file = new File(multipart.getOriginalFilename());
        multipart.transferTo(file);
        return file;
    }

    public File convert(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public static class Result {
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
