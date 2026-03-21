package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.MemberMapper;
import org.example.backend.mapper.NodeMapper;
import org.example.backend.mapper.RoleMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.model.args.CreateMemberArgs;
import org.example.backend.model.args.DeleteMemberArgs;
import org.example.backend.model.args.UpdateMemberArgs;
import org.example.backend.model.entity.Member;
import org.example.backend.model.entity.Node;
import org.example.backend.model.entity.Role;
import org.example.backend.model.entity.User;
import org.example.backend.model.view.MemberView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private NodeMapper nodeMapper;

    @Transactional
    public void createMember(CreateMemberArgs args) {
        if (args == null) throw new BusinessException("");

        // 查询用户是否存在
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getUsername, args.getUsername());
        User user = userMapper.selectOne(userQuery);
        if (user == null) throw new BusinessException("<UNK>");

        // 查询角色是否存在
        LambdaQueryWrapper<Role> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.eq(Role::getId, args.getRoleId());
        if (roleMapper.selectOne(roleQuery) == null) throw new BusinessException("<UNK>");

        // 查询组织节点是否存在
        LambdaQueryWrapper<Node> nodeQuery = new LambdaQueryWrapper<>();
        nodeQuery.eq(Node::getId, args.getNodeId());
        if (nodeMapper.selectOne(nodeQuery) == null) throw new BusinessException("<UNK>");

        int memberInsertCount = memberMapper.insert(Member.builder()
                        .userId(user.getId())
                        .nodeId(args.getNodeId())
                        .roleId(args.getRoleId())
                        .build());
        if (memberInsertCount != 1) throw new BusinessException("<UNK>");
    }

    @Transactional
    public void deleteMembers(DeleteMemberArgs args) {
        if (args == null) throw new BusinessException("");
    }

    @Transactional
    public void updateMember(UpdateMemberArgs args) {
        if (args == null) throw new BusinessException("");
    }

    public List<MemberView> listAllMembers() {

    }

    public List<Role> listOrgRoles() {

    }

    public List<Node> listOrgNodes() {

    }
}
