package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioAsyncClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.UserMapper;
import org.example.backend.model.entity.User;
import org.example.backend.model.args.ChangePasswordArgs;
import org.example.backend.model.args.UpdateProfileArgs;
import org.example.backend.model.args.UploadAvatarArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
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
    private static final long AVATAR_MAX_SIZE = 5 * 1024 * 1024;

    public User getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return user;
    }

    // TODO: 前端需要缓存预签名链接，避免每次加载都要请求后端
    public String getAvatar(Long userId) {
        // 判断用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 获取头像预处理链接
        String url = null;
        try {
             url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(AVATAR_BUCKET)
                            .object(user.getAvatar())
                            .expiry(8, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            throw new BusinessException("<UNK>");
        }

        return url;
    }

    @Transactional
    public void updateProfile(UpdateProfileArgs args) {
        // 判断用户是否存在
        User user = userMapper.selectById(args.getUserId());
        if (user == null) {
            throw new BusinessException("<UNK>");
        }

        // 更新用户信息
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(args.getEmail() != null, User::getEmail, args.getEmail())
                .set(args.getMobile() != null, User::getMobile, args.getMobile())
                .eq(User::getId, args.getUserId());

        int count = userMapper.update(updateWrapper);
        if (count != 1) {
            throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void uploadAvatar(UploadAvatarArgs args) {
        // 格式与大小判断
        String ext = getFileExtension(args.getFileName());
        if (!Arrays.asList("jpg", "jpeg", "png").contains(ext)) {
            throw new BusinessException("格式不支持");
        }
        if (args.getFileSize() >= AVATAR_MAX_SIZE) {
            throw new BusinessException("头像图片过大");
        }

        // 判断用户是否存在
        User existUser = userMapper.selectById(args.getUserId());
        if (existUser == null) {
            throw new BusinessException("<UNK>");
        }

        // 构建objectName
        String objectName = String.format("user/%d/%s.%s", args.getUserId(), UUID.randomUUID(), ext);

        // 上传头像
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
            } catch (Exception ex) {
                log.error("");
            }

            throw new BusinessException("上传头像失败");
        }

        // 更新头像信息
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getAvatar, objectName)
                .eq(User::getId, args.getUserId());

        int count = userMapper.update(updateWrapper);
        if (count != 1) {
            throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void updatePassword(ChangePasswordArgs args) {
        // 判断用户是否存在
        User user = userMapper.selectById(args.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 密码比对
        if (!passwordEncoder.matches(args.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不相同");
        }
        if (!args.getNewPassword().equals(args.getConfirmPassword())) {
            throw new BusinessException("新密码与确认密码不同");
        }

        // 更新密码
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(args.getNewPassword() != null, User::getPassword, passwordEncoder.encode(args.getNewPassword()))
                .eq(User::getId, args.getUserId());

        int count = userMapper.update(updateWrapper);
        if (count != 1) {
            throw new BusinessException("修改密码失败");
        }
    }

    private String getFileExtension(String fileName) {
        // 处理null或空字符串的情况
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        // 找到最后一个点的位置
        int lastDotIndex = fileName.lastIndexOf('.');

        // 处理没有后缀的情况（如 "README" 或 "." 开头的隐藏文件 ".gitignore"）
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        // 截取后缀并转为小写，增强通用性
        String extension = fileName.substring(lastDotIndex + 1);
        return extension.toLowerCase();
    }
}
