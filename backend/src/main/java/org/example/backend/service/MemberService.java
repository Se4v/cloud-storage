package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

import java.util.*;
import java.util.stream.Collectors;

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
        userQuery.eq(User::getUsername, args.getUsername())
                .eq(User::getEnabled, 1)
                .eq(User::getDeleted, 0);
        User user = userMapper.selectOne(userQuery);
        if (user == null) throw new BusinessException("<UNK>");

        // 查询角色是否存在
        LambdaQueryWrapper<Role> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.eq(Role::getId, args.getRoleId())
                .eq(Role::getEnabled, 1)
                .eq(Role::getDeleted, 0);
        if (roleMapper.selectOne(roleQuery) == null) throw new BusinessException("<UNK>");

        // 查询组织节点是否存在
        LambdaQueryWrapper<Node> nodeQuery = new LambdaQueryWrapper<>();
        nodeQuery.eq(Node::getId, args.getNodeId())
                .eq(Node::getEnabled, 1)
                .eq(Node::getDeleted, 0);
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

        List<Long> memberIds = args.getMemberIds();
        if (memberIds == null) throw new BusinessException("");

        LambdaUpdateWrapper<Member> memberUpdate = new LambdaUpdateWrapper<>();
        memberUpdate.in(Member::getId, memberIds);
        int memberDeleteCount = memberMapper.update(memberUpdate);
        if (memberDeleteCount != memberIds.size()) throw new BusinessException("<UNK>");
    }

    @Transactional
    public void updateMember(UpdateMemberArgs args) {
        if (args == null) throw new BusinessException("");

        // 查询成员信息是否存在
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getId, args.getMemberId()).eq(Member::getDeleted, 0);
        if (memberMapper.selectOne(memberQuery) == null) throw new BusinessException("<UNK>");

        // 更新信息
        LambdaUpdateWrapper<Member> memberUpdate = new LambdaUpdateWrapper<>();
        memberUpdate.set(args.getNodeId() != null, Member::getNodeId, args.getNodeId())
                .set(args.getRoleId() != null, Member::getRoleId, args.getRoleId())
                .eq(Member::getId, args.getMemberId());
        if (memberMapper.update(memberUpdate) != 1) throw new BusinessException("<UNK>");
    }

    public List<MemberView> listAllMembers() {
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getDeleted, 0);
        List<Member> members = memberMapper.selectList(memberQuery);
        if (members == null) return List.of();

        Set<Long> userIds = new HashSet<>();
        Set<Long> roleIds = new HashSet<>();
        Set<Long> nodeIds = new HashSet<>();
        members.forEach(member -> {
            userIds.add(member.getUserId());
            roleIds.add(member.getRoleId());
            nodeIds.add(member.getNodeId());
        });

        // 查询用户信息
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.eq(User::getEnabled, 1).eq(User::getDeleted, 0).in(User::getId, userIds);
            List<User> users = userMapper.selectList(userQuery);
            userMap.putAll(
                    users.stream().collect(Collectors.toMap(User::getId, user -> user, (k1, k2) -> k1))
            );

        }

        // 查询角色信息
        Map<Long, Role> roleMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            LambdaQueryWrapper<Role> roleQuery = new LambdaQueryWrapper<>();
            roleQuery.eq(Role::getDeleted, 0).eq(Role::getEnabled, 1).in(Role::getId, roleIds);
            List<Role> roles = roleMapper.selectList(roleQuery);
            roleMap.putAll(
                    roles.stream().collect(Collectors.toMap(Role::getId, role -> role, (k1, k2) -> k1))
            );
        }

        // 查询节点信息
        Map<Long, Node> nodeMap = new HashMap<>();
        if (!nodeIds.isEmpty()) {
            LambdaQueryWrapper<Node> nodeQuery = new LambdaQueryWrapper<>();
            nodeQuery.eq(Node::getDeleted, 0).eq(Node::getEnabled, 1).in(Node::getId, nodeIds);
            List<Node> nodes = nodeMapper.selectList(nodeQuery);
            nodeMap.putAll(
                    nodes.stream().collect(Collectors.toMap(Node::getId, node -> node, (k1, k2) -> k1))
            );
        }

        List<MemberView> memberViews = members.stream()
                .map(member -> {
                    User user = userMap.get(member.getUserId());
                    Role role = roleMap.get(member.getRoleId());
                    Node node = nodeMap.get(member.getNodeId());
                    return MemberView.builder()
                            .id(member.getId())
                            .username(user != null ? user.getUsername() : "")
                            .realName(user != null ? user.getRealName() : "")
                            .roleName(role != null ? role.getName() : "")
                            .nodeName(node != null ? node.getNodeName() : "")
                            .build();
                })
                .collect(Collectors.toList());

        return memberViews;
    }

    public List<Role> listOrgRoles() {
        LambdaQueryWrapper<Role> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.eq(Role::getType, 2)
                .eq(Role::getEnabled, 1)
                .eq(Role::getDeleted, 0);
        return roleMapper.selectList(roleQuery);
    }

    public List<Node> listOrgNodes() {
        LambdaQueryWrapper<Node> nodeQuery = new LambdaQueryWrapper<>();
        nodeQuery.eq(Node::getEnabled, 1)
                .eq(Node::getDeleted, 0);
        return nodeMapper.selectList(nodeQuery);
    }
}
