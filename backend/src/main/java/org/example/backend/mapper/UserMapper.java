package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {
    Integer updateSelectiveByUserId(User user, Long userId);

    User selectByUserId(Long id);

    User selectByUsername(String username);

    List<String> selectGlobalRolesByUserId(Long id);

}
