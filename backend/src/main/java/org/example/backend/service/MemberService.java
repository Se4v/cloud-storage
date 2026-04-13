package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.util.SecurityUtils;
import org.example.backend.mapper.MemberMapper;
import org.example.backend.mapper.NodeMapper;
import org.example.backend.mapper.RoleMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.model.request.member.MemberCreationReq;
import org.example.backend.model.request.member.MemberDeletionReq;
import org.example.backend.model.request.member.MemberUpdateReq;
import org.example.backend.model.entity.Member;
import org.example.backend.model.entity.Node;
import org.example.backend.model.entity.Role;
import org.example.backend.model.entity.User;
import org.example.backend.model.response.member.MemberResp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MemberService {
    private final MemberMapper memberMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final NodeMapper nodeMapper;

    public MemberService(MemberMapper memberMapper, UserMapper userMapper,
                         RoleMapper roleMapper, NodeMapper nodeMapper) {
        this.memberMapper = memberMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.nodeMapper = nodeMapper;
    }

    @Transactional
    public void createMember(MemberCreationReq req) {
        // 查询用户是否存在
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUsername, req.getUsername())
                        .eq(User::getEnabled, DbConsts.ENABLED_YES)
                        .eq(User::getDeleted, DbConsts.DELETED_NO));
        if (user == null) throw new BusinessException("<UNK>");

        // 查询角色是否存在
        Role role = roleMapper.selectOne(
                Wrappers.<Role>lambdaQuery()
                        .eq(Role::getId, req.getRoleId())
                        .eq(Role::getEnabled, DbConsts.ENABLED_YES)
                        .eq(Role::getDeleted, DbConsts.DELETED_NO));
        if (role == null) throw new BusinessException("<UNK>");

        // 查询组织节点是否存在
        Node node = nodeMapper.selectOne(
                Wrappers.<Node>lambdaQuery()
                        .eq(Node::getId, req.getNodeId())
                        .eq(Node::getIsEnabled, DbConsts.ENABLED_YES)
                        .eq(Node::getIsDeleted, DbConsts.DELETED_NO));
        if (node == null) throw new BusinessException("<UNK>");

        Member member = Member.builder()
                .userId(user.getId())
                .nodeId(req.getNodeId())
                .roleId(req.getRoleId())
                .build();
        int count = memberMapper.insert(member);
        if (count != 1) throw new BusinessException("<UNK>");
    }

    @Transactional
    public void deleteMembers(MemberDeletionReq req) {
        int count = memberMapper.update(
                Wrappers.<Member>lambdaUpdate()
                        .set(Member::getDeleted, DbConsts.DELETED_YES)
                        .in(Member::getId, req.getMemberIds()));
        if (count != req.getMemberIds().size()) throw new BusinessException("<UNK>");
    }

    @Transactional
    public void updateMember(MemberUpdateReq req) {
        // 查询成员信息是否存在
        Member member = memberMapper.selectOne(
                Wrappers.<Member>lambdaQuery()
                        .eq(Member::getId, req.getMemberId())
                        .eq(Member::getDeleted, DbConsts.DELETED_NO));
        if (member == null) throw new BusinessException("<UNK>");

        // 更新信息
        int count = memberMapper.update(
                Wrappers.<Member>lambdaUpdate()
                        .set(Member::getNodeId, req.getNodeId())
                        .set(Member::getRoleId, req.getRoleId())
                        .eq(Member::getId, req.getMemberId()));
        if (count != 1) throw new BusinessException("<UNK>");
    }

    public List<MemberResp> listAllMembers() {
        List<Long> manageNodeIds = null;
        if (!SecurityUtils.isSuperAdmin()) {
            manageNodeIds = SecurityUtils.getManageNodeIds();
        }

        List<Member> members = memberMapper.selectList(
                Wrappers.<Member>lambdaQuery()
                        .eq(Member::getDeleted, DbConsts.DELETED_NO)
                        .in(manageNodeIds != null && !manageNodeIds.isEmpty(), Member::getNodeId, manageNodeIds));
        if (members == null || members.isEmpty()) return List.of();

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
            List<User> users = userMapper.selectList(
                    Wrappers.<User>lambdaQuery()
                            .eq(User::getEnabled, DbConsts.ENABLED_YES)
                            .eq(User::getDeleted, DbConsts.DELETED_NO)
                            .in(User::getId, userIds));
            userMap.putAll(
                    users.stream().collect(Collectors.toMap(User::getId, user -> user, (k1, k2) -> k1))
            );

        }

        // 查询角色信息
        Map<Long, Role> roleMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            List<Role> roles = roleMapper.selectList(
                    Wrappers.<Role>lambdaQuery()
                            .eq(Role::getEnabled, DbConsts.ENABLED_YES)
                            .eq(Role::getDeleted, DbConsts.DELETED_NO)
                            .in(Role::getId, roleIds));
            roleMap.putAll(
                    roles.stream().collect(Collectors.toMap(Role::getId, role -> role, (k1, k2) -> k1))
            );
        }

        // 查询节点信息
        Map<Long, Node> nodeMap = new HashMap<>();
        if (!nodeIds.isEmpty()) {
            List<Node> nodes = nodeMapper.selectList(
                    Wrappers.<Node>lambdaQuery()
                            .eq(Node::getIsEnabled, DbConsts.ENABLED_YES)
                            .eq(Node::getIsDeleted, DbConsts.DELETED_NO)
                            .in(Node::getId, nodeIds));
            nodeMap.putAll(
                    nodes.stream().collect(Collectors.toMap(Node::getId, node -> node, (k1, k2) -> k1))
            );
        }

        List<MemberResp> resp = members.stream()
                .map(member -> {
                    User user = userMap.get(member.getUserId());
                    Role role = roleMap.get(member.getRoleId());
                    Node node = nodeMap.get(member.getNodeId());
                    return MemberResp.builder()
                            .id(member.getId())
                            .username(user != null ? user.getUsername() : "")
                            .realName(user != null ? user.getRealName() : "")
                            .roleName(role != null ? role.getName() : "")
                            .nodeName(node != null ? node.getNodeName() : "")
                            .build();
                })
                .toList();

        return resp;
    }

    public List<Role> listOrgRoles() {
        List<Role> orgRoles = roleMapper.selectList(
                Wrappers.<Role>lambdaQuery()
                        .eq(Role::getType, DbConsts.ROLE_TYPE_ORG)
                        .eq(Role::getEnabled, DbConsts.ENABLED_YES)
                        .eq(Role::getDeleted, DbConsts.DELETED_NO));
        return orgRoles;
    }

    public List<Node> listOrgNodes() {
        List<Node> orgNodes = nodeMapper.selectList(
                Wrappers.<Node>lambdaQuery()
                        .eq(Node::getIsEnabled, DbConsts.ENABLED_YES)
                        .eq(Node::getIsDeleted, DbConsts.DELETED_NO));
        return orgNodes;
    }
}