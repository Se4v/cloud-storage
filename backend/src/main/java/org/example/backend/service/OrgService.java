package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.util.SecurityUtils;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.MemberMapper;
import org.example.backend.mapper.NodeMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.model.request.notice.NodeCreationReq;
import org.example.backend.model.request.org.OrgNodeDeletionReq;
import org.example.backend.model.request.org.OrgNodeUpdateReq;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Member;
import org.example.backend.model.entity.Node;
import org.example.backend.model.entity.User;
import org.example.backend.model.response.org.OrgNodeResp;
import org.example.backend.model.response.org.OrgNodeTreeResp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrgService {
    private final MemberMapper memberMapper;
    private final NodeMapper nodeMapper;
    private final DriveMapper driveMapper;
    private final UserMapper userMapper;

    private static final int DELETED = 1;

    public OrgService(MemberMapper memberMapper, NodeMapper nodeMapper,
                      DriveMapper driveMapper, UserMapper userMapper) {
        this.memberMapper = memberMapper;
        this.nodeMapper = nodeMapper;
        this.driveMapper = driveMapper;
        this.userMapper = userMapper;
    }

    public List<OrgNodeTreeResp> getOrgTree() {
        Long currentUserId = SecurityUtils.getUserId();
        List<Member> members = memberMapper.selectList(
                Wrappers.<Member>lambdaQuery()
                        .eq(Member::getUserId, currentUserId));

        List<Long> childNodeIds = members.stream().map(Member::getNodeId).toList();
        List<Node> nodeList = nodeMapper.selectNodeWithParents(childNodeIds);
        List<Long> allNodeIds = nodeList.stream().map(Node::getId).toList();

        List<Drive> driveList = driveMapper.selectList(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getDriveType, 2)
                        .in(Drive::getNodeId, allNodeIds));
        Map<Long, Long> driveMap = driveList.stream().collect(Collectors.toMap(Drive::getNodeId, Drive::getId));

        // 构建节点ID到节点的映射
        Map<Long, Node> nodeMap = nodeList.stream().collect(Collectors.toMap(Node::getId, node -> node));
        
        // 找出根节点（parentId为0或父节点不在nodeList中的节点）
        List<Node> rootNodes = nodeList.stream()
                .filter(node -> node.getParentId() == 0 || !nodeMap.containsKey(node.getParentId()))
                .toList();
        
        // 递归构建树形结构
        List<OrgNodeTreeResp> orgTree = rootNodes.stream()
                .map(node -> buildOrgTree(node, nodeMap, driveMap))
                .toList();
        
        return orgTree;
    }

    private OrgNodeTreeResp buildOrgTree(Node node, Map<Long, Node> nodeMap, Map<Long, Long> driveMap) {
        // 获取子节点
        List<Node> children = nodeMap.values().stream()
                .filter(n -> node.getId().equals(n.getParentId()))
                .toList();
        
        // 递归构建子节点树
        List<OrgNodeTreeResp> resp = children.stream()
                .map(child -> buildOrgTree(child, nodeMap, driveMap))
                .toList();
        
        return OrgNodeTreeResp.builder()
                .id(node.getId())
                .driveId(driveMap.get(node.getId()))
                .name(node.getNodeName())
                .type(node.getNodeType())
                .children(resp)
                .build();
    }

    @Transactional
    public void createOrgNode(NodeCreationReq req) {
        // 校验父节点是否存在（如果不是根节点）
        if (req.getParentId() > 0) {
            Node parentNode = nodeMapper.selectById(req.getParentId());
            if (parentNode == null) throw new BusinessException("父节点不存在");
        }

        // 校验同一父节点下名称是否重复
        long sameName = nodeMapper.selectCount(
                Wrappers.<Node>lambdaQuery()
                        .eq(Node::getParentId, req.getParentId())
                        .eq(Node::getNodeName, req.getName()));
        if (sameName > 0) throw new BusinessException("同一父节点下已存在相同名称的部门");

        // 创建节点
        Node node = Node.builder()
                .nodeName(req.getName())
                .nodeType(req.getType())
                .parentId(req.getParentId())
                .build();
        int nodeInsert = nodeMapper.insert(node);
        if (nodeInsert != 1) throw new BusinessException("创建部门失败");

        // 查询节点管理员
        Long adminId;
        if (req.getAdminUsername().isBlank()) {
            adminId = 0L;
        } else {
            User admin = userMapper.selectOne(
                    Wrappers.<User>lambdaQuery()
                            .eq(User::getUsername, req.getAdminUsername()));
            if (admin == null) throw new BusinessException("该用户不存在");
            adminId = admin.getId();
        }

        // 创建企业空间
        Drive drive = Drive.builder()
                .driveName(req.getName())
                .driveType(2)
                .nodeId(node.getId())
                .userId(adminId)
                .totalQuota(req.getStorageQuota())
                .build();

        int driveInsert = driveMapper.insert(drive);
        if (driveInsert != 1) throw new BusinessException("创建部门失败");
    }

    @Transactional
    public void deleteOrgNodes(OrgNodeDeletionReq req) {
        List<Long> nodeIds = req.getNodeIds();

        // 校验是否存在子节点
        long childCount = nodeMapper.selectCount(
                Wrappers.<Node>lambdaQuery()
                        .in(Node::getParentId, nodeIds));
        if (childCount > 0) throw new BusinessException("请先删除该节点下的所有子节点");

        // 删除节点
        int updateCount = nodeMapper.update(
                Wrappers.<Node>lambdaUpdate()
                        .set(Node::getIsDeleted, DELETED)
                        .in(Node::getId, nodeIds));
        if (updateCount != nodeIds.size()) throw new BusinessException("删除节点失败");
    }

    @Transactional
    public void updateOrgNode(OrgNodeUpdateReq req) {
        // 校验节点是否存在
        Node existingNode = nodeMapper.selectById(req.getId());
        if (existingNode == null) throw new BusinessException("节点不存在");

        // 校验父节点是否合法
        if (req.getParentId() >= 0) {
            if (req.getParentId().equals(req.getId())) throw new BusinessException("不能设置自己为父节点");
            List<Node> children = nodeMapper.selectList(
                    Wrappers.<Node>lambdaQuery()
                            .eq(Node::getParentId, req.getId())
                            .eq(Node::getIsDeleted, 0));
            Map<Long, Node> childrenMap = children.stream().collect(Collectors.toMap(Node::getId, node -> node));
            if (childrenMap.containsKey(req.getParentId())) throw new BusinessException("不能设置自己的子节点为父节点");
        }

        // 校验名称是否重复（如果名称有变更）
        if (!req.getName().equals(existingNode.getNodeName())) {
            Long parentId = Objects.equals(req.getParentId(), existingNode.getParentId())
                    ? existingNode.getParentId() : req.getParentId();
            long sameName = nodeMapper.selectCount(
                    Wrappers.<Node>lambdaQuery()
                            .eq(Node::getParentId, parentId)
                            .eq(Node::getNodeName, req.getName())
                            .ne(Node::getId, req.getId()));
            if (sameName > 0) throw new BusinessException("同一父节点下已存在相同名称的节点");
        }

        // 更新组织节点
        int nodeUpdateCount = nodeMapper.update(
                Wrappers.<Node>lambdaUpdate()
                        .set(Node::getNodeName, req.getName())
                        .set(Node::getNodeType, req.getType())
                        .set(Node::getParentId, req.getParentId())
                        .set(Node::getIsEnabled, req.getIsEnabled())
                        .eq(Node::getId, req.getId()));
        if (nodeUpdateCount != 1) throw new BusinessException("更新节点失败");

        // 校验节点管理员是否存在
        Long adminId;
        if (req.getAdminUsername().isBlank()) {
            adminId = 0L;
        } else {
            User admin = userMapper.selectOne(
                    Wrappers.<User>lambdaQuery()
                            .eq(User::getUsername, req.getAdminUsername()));
            if (admin == null) throw new BusinessException("该用户不存在");
            adminId = admin.getId();
        }

        // 更新
        Drive drive = driveMapper.selectOne(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getNodeId, req.getId()));
        if (drive == null) throw new BusinessException("节点对应的存储空间不存在");
        if (drive.getUsedQuota() > req.getStorageQuota()) throw new BusinessException("");

        int driveUpdateCount = driveMapper.update(
                Wrappers.<Drive>lambdaUpdate()
                        .set(Drive::getDriveName, req.getName())
                        .set(Drive::getTotalQuota, req.getStorageQuota())
                        .set(Drive::getUserId, adminId)
                        .eq(Drive::getNodeId, drive.getNodeId()));
        if (driveUpdateCount != 1) throw new BusinessException("更新节点-空间失败");
    }

    public List<OrgNodeResp> listAllOrgNodes() {
        // 查询组织节点
        List<Node> nodeList = nodeMapper.selectList(
                Wrappers.<Node>lambdaQuery()
                        .eq(Node::getIsDeleted, 0));
        if (nodeList == null || nodeList.isEmpty()) return List.of();
        Map<Long, String> parentNodeMap = nodeList.stream().collect(Collectors.toMap(Node::getId, Node::getNodeName));

        // 查询空间
        List<Long> nodeIdList = nodeList.stream()
                .map(Node::getId)
                .toList();
        List<Drive> driveList = driveMapper.selectList(
                Wrappers.<Drive>lambdaQuery()
                        .in(Drive::getNodeId, nodeIdList));
        Map<Long, Drive> driveMap = driveList.stream()
                .collect(Collectors.toMap(Drive::getNodeId, drive -> drive));

        // 查询用户
        Set<Long> adminIdList = driveList.stream()
                .map(Drive::getUserId)
                .collect(Collectors.toSet());
        List<User> userList = userMapper.selectList(
                Wrappers.<User>lambdaQuery()
                        .in(User::getId, adminIdList));
        Map<Long, String> userMap = userList.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        List<OrgNodeResp> resp = nodeList.stream()
                .map(node -> {
                    String parentName = parentNodeMap.getOrDefault(node.getParentId(), "根节点");
                    Drive drive = driveMap.get(node.getId());
                    Long storageQuota = drive.getTotalQuota();
                    String adminUsername = userMap.getOrDefault(drive.getUserId(), "");

                    return OrgNodeResp.builder()
                            .id(node.getId())
                            .name(node.getNodeName())
                            .type(node.getNodeType())
                            .parentId(node.getParentId())
                            .parentName(parentName)
                            .storageQuota(storageQuota)
                            .isEnabled(node.getIsEnabled())
                            .adminUsername(adminUsername)
                            .build();
                })
                .toList();

        return resp;
    }
}
