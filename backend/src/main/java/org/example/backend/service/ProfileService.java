package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioAsyncClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.UserMapper;
import org.example.backend.model.request.user.AvatarUpdateReq;
import org.example.backend.model.entity.User;
import org.example.backend.model.request.user.PasswordChangeReq;
import org.example.backend.model.request.user.ProfileUpdateReq;
import org.example.backend.model.response.user.AvatarUploadResp;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ProfileService {
    private final UserMapper userMapper;
    private final MinioAsyncClient minioClient;
    private final PasswordEncoder passwordEncoder;

    private static final String AVATAR_BUCKET = "avatars";
    private static final long AVATAR_MAX_SIZE = 5 * 1024 * 1024;

    public ProfileService(UserMapper userMapper, MinioAsyncClient minioClient, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.minioClient = minioClient;
        this.passwordEncoder = passwordEncoder;
    }

    public String getAvatar(Long userId) {
        // 判断用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        // 获取头像预处理链接
        String url;
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

    public AvatarUploadResp getAvatarUploadUrl(String fileName, Long fileSize, Long userId) {
        // 判断用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        // 校验文件大小
        if (fileSize != null && fileSize > AVATAR_MAX_SIZE) throw new BusinessException("头像图片过大，请上传5MB以内的图片");

        // 校验文件格式
        String ext = getFileExtension(fileName);
        if (!Arrays.asList("jpg", "jpeg", "png").contains(ext)) throw new BusinessException("格式不支持");

        String objectName = String.format("user/%d/%s.%s", userId, UUID.randomUUID(), ext);

        try {
            // 生成预签名 PUT 链接
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(AVATAR_BUCKET)
                            .object(objectName)
                            .expiry(10, TimeUnit.MINUTES)
                            .build()
            );

            // 返回结果
            return AvatarUploadResp.builder()
                    .uploadUrl(presignedUrl)
                    .objectName(objectName)
                    .build();
        } catch (Exception e) {
            log.error("生成 MinIO 上传链接失败", e);
            throw new BusinessException("获取上传链接失败");
        }
    }

    @Transactional
    public void updateAvatar(AvatarUpdateReq req, Long userId) {
        // 判断用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        // 更新头像信息
        int count = userMapper.update(
                Wrappers.<User>lambdaUpdate()
                        .set(User::getAvatar, req.getObjectName())
                        .eq(User::getId, user.getId()));
        if (count != 1) throw new BusinessException("<UNK>");
    }

    public User getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }

    @Transactional
    public void updateProfile(ProfileUpdateReq req, Long userId) {
        // 判断用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("<UNK>");

        // 更新用户信息
        int count = userMapper.update(
                Wrappers.<User>lambdaUpdate()
                        .set(User::getEmail, req.getEmail())
                        .set(User::getMobile, req.getMobile())
                        .eq(User::getId, user.getId()));
        if (count != 1) throw new BusinessException("<UNK>");
    }

    @Transactional
    public void updatePassword(PasswordChangeReq req, Long userId) {
        // 判断用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        // 密码比对
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不相同");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BusinessException("新密码与确认密码不同");
        }

        // 更新密码
        int count = userMapper.update(
                Wrappers.<User>lambdaUpdate()
                        .set(User::getPassword, passwordEncoder.encode(req.getNewPassword()))
                        .eq(User::getId, user.getId()));
        if (count != 1) throw new BusinessException("修改密码失败");
    }

    private String getFileExtension(String fileName) {
        // 处理null或空字符串的情况
        if (fileName == null || fileName.isEmpty()) return "";

        // 找到最后一个点的位置
        int lastDotIndex = fileName.lastIndexOf('.');

        // 处理没有后缀的情况（如 "README" 或 "." 开头的隐藏文件 ".gitignore"）
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) return "";

        // 截取后缀并转为小写，增强通用性
        String extension = fileName.substring(lastDotIndex + 1);
        return extension.toLowerCase();
    }
}
