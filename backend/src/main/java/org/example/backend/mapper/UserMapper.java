package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.example.backend.model.entity.User;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<User> {
    List<String> selectSystemRoles(@Param("userId") Long userId);

    List<String> selectSystemPermissions(@Param("userId") Long userId);

    List<Map<String, Object>> selectOrgRoles(@Param("userId") Long userId);

    List<Map<String, Object>> selectOrgPermissions(@Param("userId") Long userId);
}
