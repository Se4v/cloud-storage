package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.aspect.LogContextHolder;
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

    /**
     * 创建成员
     * @param req 创建成员的请求参数
     */
    @Transactional
    public void createMember(MemberCreationReq req) {
        // 查询用户是否存在
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUsername, req.getUsername())
                        .eq(User::getEnabled, DbConsts.ENABLED_YES)
                        .eq(User::getDeleted, DbConsts.DELETED_NO));
        if (user == null) throw new BusinessException("用户不存在");

        // 查询角色是否存在
        Role role = roleMapper.selectOne(
                Wrappers.<Role>lambdaQuery()
                        .eq(Role::getId, req.getRoleId())
                        .eq(Role::getEnabled, DbConsts.ENABLED_YES)
                        .eq(Role::getDeleted, DbConsts.DELETED_NO));
        if (role == null) throw new BusinessException("创建成员失败");

        // 查询组织节点是否存在
        Node node = nodeMapper.selectOne(
                Wrappers.<Node>lambdaQuery()
                        .eq(Node::getId, req.getNodeId())
                        .eq(Node::getIsEnabled, DbConsts.ENABLED_YES)
                        .eq(Node::getIsDeleted, DbConsts.DELETED_NO));
        if (node == null) throw new BusinessException("创建成员失败");

        LogContextHolder.setTargetId(0L);
        LogContextHolder.setTargetName("创建成员");
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("userId", user.getId());
        logMap.put("username", user.getUsername());
        logMap.put("roleId", role.getId());
        logMap.put("roleName", role.getName());
        logMap.put("nodeId", node.getId());
        logMap.put("nodeName",node.getNodeName());
        LogContextHolder.addDetailProperty("member_create", logMap);

        Member member = Member.builder()
                .userId(user.getId())
                .nodeId(req.getNodeId())
                .roleId(req.getRoleId())
                .build();
        int count = memberMapper.insert(member);
        if (count != 1) throw new BusinessException("创建成员失败");
    }

    /**
     * 删除成员
     * @param req 包含要删除的成员ID列表的请求
     */
    @Transactional
    public void deleteMembers(MemberDeletionReq req) {
        LogContextHolder.setTargetId(0L);
        LogContextHolder.setTargetName("批量删除" + req.getMemberIds().size() + "个成员");
        List<Member> members = memberMapper.selectList(Wrappers.<Member>lambdaQuery().in(Member::getId, req.getMemberIds()));
        List<Map<String, Object>> logs = members.stream()
                .map(member -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", member.getId());
                    map.put("userid", member.getUserId());
                    map.put("roleId", member.getRoleId());
                    map.put("nodeId", member.getNodeId());
                    return map;
                })
                .toList();
        LogContextHolder.addDetailProperty("member_delete", logs);

        int count = memberMapper.update(
                Wrappers.<Member>lambdaUpdate()
                        .set(Member::getDeleted, DbConsts.DELETED_YES)
                        .in(Member::getId, req.getMemberIds()));
        if (count != req.getMemberIds().size()) throw new BusinessException("删除成员失败");
    }

    /**
     * 更新成员信息
     * @param req 更新成员的请求参数
     */
    @Transactional
    public void updateMember(MemberUpdateReq req) {
        // 查询成员信息是否存在
        Member member = memberMapper.selectOne(
                Wrappers.<Member>lambdaQuery()
                        .eq(Member::getId, req.getMemberId())
                        .eq(Member::getDeleted, DbConsts.DELETED_NO));
        if (member == null) throw new BusinessException("更新成员信息失败");

        LogContextHolder.setTargetId(member.getId());
        LogContextHolder.setTargetName("更新成员信息");
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("userId", member.getUserId());
        logMap.put("oldRoleId", member.getRoleId());
        logMap.put("newRoleId", req.getRoleId());
        logMap.put("oldNodeId", member.getNodeId());
        logMap.put("newNodeId", req.getNodeId());
        LogContextHolder.addDetailProperty("member_update", logMap);

        // 更新信息
        int count = memberMapper.update(
                Wrappers.<Member>lambdaUpdate()
                        .set(Member::getNodeId, req.getNodeId())
                        .set(Member::getRoleId, req.getRoleId())
                        .eq(Member::getId, req.getMemberId()));
        if (count != 1) throw new BusinessException("更新成员信息失败");
    }

    /**
     * 列出所有成员
     * @return 成员响应列表
     */
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

    /**
     * 列出所有组织角色
     * @return 组织角色列表
     */
    public List<Role> listOrgRoles() {
        List<Role> orgRoles = roleMapper.selectList(
                Wrappers.<Role>lambdaQuery()
                        .eq(Role::getType, DbConsts.ROLE_TYPE_ORG)
                        .eq(Role::getEnabled, DbConsts.ENABLED_YES)
                        .eq(Role::getDeleted, DbConsts.DELETED_NO));
        return orgRoles;
    }

    /**
     * 列出所有组织节点
     * @return 组织节点列表
     */
    public List<Node> listOrgNodes() {
        List<Node> orgNodes = nodeMapper.selectList(
                Wrappers.<Node>lambdaQuery()
                        .eq(Node::getIsEnabled, DbConsts.ENABLED_YES)
                        .eq(Node::getIsDeleted, DbConsts.DELETED_NO));
        return orgNodes;
    }
}