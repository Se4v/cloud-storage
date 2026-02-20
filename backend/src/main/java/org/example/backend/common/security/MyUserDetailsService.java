package org.example.backend.common.security;

import org.example.backend.mapper.UserMapper;
import org.example.backend.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null || user.getEnabled() == 0) {
            throw new UsernameNotFoundException("用户不存在/用户禁用");
        }

        List<String> roles = userMapper.selectRolesByUserId(user.getId());
        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());

        return new MyUserDetails(user.getId(), user.getUsername(), user.getPassword(), roles, permissions, null);
    }
}
