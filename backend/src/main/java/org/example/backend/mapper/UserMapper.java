package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {
    List<String> selectGlobalRolesByUserId(Long id);
}
