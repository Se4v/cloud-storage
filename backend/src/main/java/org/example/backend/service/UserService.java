package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.ConfigMapper;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.model.args.CreateUserArgs;
import org.example.backend.model.args.DeleteUserArgs;
import org.example.backend.model.args.UpdateUserArgs;
import org.example.backend.model.entity.Config;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConfigMapper configMapper;
    @Autowired
    private DriveMapper driveMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int ENABLED = 1;
    private static final int DISABLED = 0;
    private static final int UNDELETED = 0;
    private static final int DELETED = 1;

    /**
     * 创建用户
     */
    @Transactional
    public void createUser(CreateUserArgs args) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getUsername, args.getUsername())
                .eq(User::getDeleted, UNDELETED);
        User existUser = userMapper.selectOne(userQuery);
        if (existUser != null) throw new BusinessException("用户名已存在");

        // 调用配置表
        String defaultPassword;
        LambdaQueryWrapper<Config> configQuery = new LambdaQueryWrapper<>();
        configQuery.eq(Config::getEnabled, ENABLED)
                .eq(Config::getKey, "default_password");
        Config config = configMapper.selectOne(configQuery);
        if (config == null || config.getValue().isEmpty()) defaultPassword = "12345";
        else defaultPassword = config.getValue();

        // 创建用户
        User user = User.builder()
                .username(args.getUsername())
                .password(passwordEncoder.encode(defaultPassword))
                .realName(args.getRealName())
                .mobile(args.getMobile())
                .build();

        int userCount = userMapper.insert(user);
        if (userCount != 1) throw new BusinessException("创建用户失败");

        Drive drive = Drive.builder()
                .driveName("个人空间")
                .driveType(4)
                .nodeId(0L)
                .userId(user.getId())
                .totalQuota(args.getStorageQuota())
                .usedQuota(0L)
                .build();

        int driveCount = driveMapper.insert(drive);
        if (driveCount != 1) throw new BusinessException("<UNK>");
    }

    /**
     * 批量删除用户（逻辑删除）
     */
    @Transactional
    public void deleteUsers(DeleteUserArgs args) {
        if (args.getIds() == null || args.getIds().isEmpty()) {
            throw new BusinessException("请选择要删除的用户");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(User::getId, args.getIds())
                .set(User::getDeleted, DELETED)
                .set(User::getUpdatedAt, LocalDateTime.now());

        int count = userMapper.update(updateWrapper);
        if (count != args.getIds().size()) {
            throw new BusinessException("删除用户失败");
        }
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUser(UpdateUserArgs args) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(args.getRealName() != null, User::getRealName, args.getRealName())
                .set(args.getMobile() != null, User::getMobile, args.getMobile())
                .set(args.getEmail() != null, User::getEmail, args.getEmail())
                .set(args.getEnabled() != null, User::getEnabled, args.getEnabled())
                .set(User::getUpdatedAt, LocalDateTime.now())
                .eq(User::getId, args.getId())
                .eq(User::getDeleted, UNDELETED);

        int count = userMapper.update(updateWrapper);
        if (count != 1) {
            throw new BusinessException("更新用户失败");
        }
    }

    /**
     * 查询所有未删除的用户
     */
    public List<User> listAllUsers() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getDeleted, UNDELETED)
                .orderByDesc(User::getCreatedAt);

        return userMapper.selectList(queryWrapper);
    }
}
