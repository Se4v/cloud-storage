package org.example.backend.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioAsyncClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.UserMapper;
import org.example.backend.model.entity.User;
import org.example.backend.model.args.ChangePasswordArgs;
import org.example.backend.model.args.UpdateProfileArgs;
import org.example.backend.model.args.UploadAvatarArgs;
import org.example.backend.model.view.ProfileView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ProfileService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MinioAsyncClient minioClient;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AVATAR_BUCKET = "avatars";

    public ProfileView getProfile(Long userId) {
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return ProfileView.builder()
                .userId(userId)
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .build();
    }

    // TODO: 前端需要缓存预签名链接，避免每次加载都要请求后端
    public String getAvatar(Long userId) {
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String url = null;
        try {
             url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(AVATAR_BUCKET)
                            .object(user.getAvatar())
                            .expiry(8, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException("<UNK>");
        }

        return url;
    }

    @Transactional
    public void updateProfile(UpdateProfileArgs args, Long userId) {
        User user = User.builder()
                .mobile(args.getMobile())
                .email(args.getEmail())
                .updaterId(userId)
                .build();

        int count = userMapper.updateSelectiveByUserId(user, userId);
        if (count != 1) {
            throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void uploadAvatar(UploadAvatarArgs args, Long userId) {
        String ext = args.getFileName().substring(args.getFileName().lastIndexOf(".") + 1).toLowerCase();

        if (!Arrays.asList("jpg", "jpeg", "png").contains(ext)) {
            throw new BusinessException("格式不支持");
        }

        String objectName = String.format("user/%d/%s.%s", userId, UUID.randomUUID(), ext);

        try (InputStream inputStream = args.getFile().getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                            .bucket(AVATAR_BUCKET)
                            .object(objectName)
                            .stream(inputStream, args.getFileSize(), -1)
                            .contentType(args.getMimeType())
                            .build());
        } catch (Exception e) {
            log.error("<UNK>", e);

            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(AVATAR_BUCKET)
                                .object(objectName)
                                .build()
                );
            } catch (InsufficientDataException | InternalException | InvalidKeyException | IOException |
                     NoSuchAlgorithmException | XmlParserException | IllegalArgumentException ex) {
                log.error("");
            }

            throw new BusinessException("");
        }

        User user = User.builder()
                .avatar(objectName)
                .updaterId(userId)
                .build();

        int count = userMapper.updateSelectiveByUserId(user, userId);
        if (count != 1) {
            throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void changePassword(ChangePasswordArgs args, Long userId) {
        User user = userMapper.selectByUserId(userId);

        if (!passwordEncoder.matches(args.getOldPassword(), user.getPassword())) {
            throw new BusinessException("<UNK>");
        }
        if (!args.getNewPassword().equals(args.getConfirmPassword())) {
            throw new BusinessException("<UNK>");
        }

        User updateUser = User.builder()
                .password(passwordEncoder.encode(args.getNewPassword()))
                .updaterId(userId)
                .build();

        int count = userMapper.updateSelectiveByUserId(user, userId);
        if (count != 1) {
            throw new BusinessException("<UNK>");
        }
    }
}
