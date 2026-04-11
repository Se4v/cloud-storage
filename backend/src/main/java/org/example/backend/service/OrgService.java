package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.MemberMapper;
import org.example.backend.mapper.NodeMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.model.request.CreateNodeArgs;
import org.example.backend.model.request.DeleteNodeArgs;
import org.example.backend.model.request.UpdateNodeArgs;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Member;
import org.example.backend.model.entity.Node;
import org.example.backend.model.entity.User;
import org.example.backend.model.response.NodeView;
import org.example.backend.model.response.OrgTreeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrgService {
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private NodeMapper nodeMapper;
    @Autowired
    private DriveMapper driveMapper;
    @Autowired
    private UserMapper userMapper;

    private static final int DELETED = 1;

    public List<OrgTreeView> getOrgTree(Long userId) {
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Member::getUserId, userId);
        List<Member> members = memberMapper.selectList(queryWrapper);

        List<Long> childNodeIds = members.stream().map(Member::getNodeId).toList();
        List<Node> nodeList = nodeMapper.selectNodeWithParents(childNodeIds);
        List<Long> allNodeIds = nodeList.stream().map(Node::getId).toList();

        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getDriveType, 2).in(Drive::getNodeId, allNodeIds);
        List<Drive> driveList = driveMapper.selectList(driveQuery);
        Map<Long, Long> driveMap = driveList.stream().collect(Collectors.toMap(Drive::getNodeId, Drive::getId));

        // 构建节点ID到节点的映射
        Map<Long, Node> nodeMap = nodeList.stream().collect(Collectors.toMap(Node::getId, node -> node));
        
        // 找出根节点（parentId为0或父节点不在nodeList中的节点）
        List<Node> rootNodes = nodeList.stream()
                .filter(node -> node.getParentId() == 0 || !nodeMap.containsKey(node.getParentId()))
                .toList();
        
        // 递归构建树形结构
        List<OrgTreeView> orgTree = rootNodes.stream()
                .map(node -> buildOrgTree(node, nodeMap, driveMap))
                .toList();
        
        return orgTree;
    }

    private OrgTreeView buildOrgTree(Node node, Map<Long, Node> nodeMap, Map<Long, Long> driveMap) {
        // 获取子节点
        List<Node> children = nodeMap.values().stream()
                .filter(n -> node.getId().equals(n.getParentId()))
                .toList();
        
        // 递归构建子节点树
        List<OrgTreeView> childViews = children.stream()
                .map(child -> buildOrgTree(child, nodeMap, driveMap))
                .toList();
        
        return OrgTreeView.builder()
                .id(node.getId())
                .driveId(driveMap.get(node.getId()))
                .name(node.getNodeName())
                .type(node.getNodeType())
                .children(childViews)
                .build();
    }

    @Transactional
    public void createOrgNode(CreateNodeArgs args) {
        // 校验父节点是否存在（如果不是根节点）
        if (args.getParentId() > 0) {
            Node parentNode = nodeMapper.selectById(args.getParentId());
            if (parentNode == null) throw new BusinessException("父节点不存在");
        }

        // 校验同一父节点下名称是否重复
        LambdaQueryWrapper<Node> parentQuery = new LambdaQueryWrapper<>();
        parentQuery.eq(Node::getParentId, args.getParentId()).eq(Node::getNodeName, args.getName());
        if (nodeMapper.selectCount(parentQuery) > 0) throw new BusinessException("同一父节点下已存在相同名称的部门");

        // 创建节点
        Node node = Node.builder()
                .nodeName(args.getName())
                .nodeType(args.getType())
                .parentId(args.getParentId())
                .build();
        int nodeInsert = nodeMapper.insert(node);
        if (nodeInsert != 1) throw new BusinessException("创建部门失败");

        // 查询节点管理员
        Long adminId;
        if (args.getAdminUsername().isBlank()) {
            adminId = 0L;
        } else {
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.eq(User::getUsername, args.getAdminUsername());
            User admin = userMapper.selectOne(userQuery);
            if (admin == null) throw new BusinessException("该用户不存在");
            adminId = admin.getId();
        }


        // 创建企业空间
        Drive drive = Drive.builder()
                .driveName(args.getName())
                .driveType(2)
                .nodeId(node.getId())
                .userId(adminId)
                .totalQuota(args.getStorageQuota())
                .build();

        int driveInsert = driveMapper.insert(drive);
        if (driveInsert != 1) throw new BusinessException("创建部门失败");
    }

    @Transactional
    public void deleteOrgNodes(DeleteNodeArgs args) {
        List<Long> nodeIds = args.getNodeIds();

        // 校验是否存在子节点
        LambdaQueryWrapper<Node> childQuery = new LambdaQueryWrapper<>();
        childQuery.in(Node::getParentId, nodeIds);
        if (nodeMapper.selectCount(childQuery) > 0) throw new BusinessException("请先删除该节点下的所有子节点");

        // 删除节点
        LambdaUpdateWrapper<Node> nodeWrapper = new LambdaUpdateWrapper<>();
        nodeWrapper.set(Node::getIsDeleted, DELETED).in(Node::getId, nodeIds);
        if (nodeMapper.update(nodeWrapper) != nodeIds.size()) throw new BusinessException("删除节点失败");
    }

    @Transactional
    public void updateOrgNode(UpdateNodeArgs args) {
        // 校验节点是否存在
        Node existingNode = nodeMapper.selectById(args.getId());
        if (existingNode == null) throw new BusinessException("节点不存在");

        // 校验父节点是否合法
        if (args.getParentId() >= 0) {
            if (args.getParentId().equals(args.getId())) throw new BusinessException("不能设置自己为父节点");
            LambdaQueryWrapper<Node> childrenQuery = new LambdaQueryWrapper<>();
            childrenQuery.eq(Node::getParentId, args.getId()).eq(Node::getIsDeleted, 0);
            List<Node> children = nodeMapper.selectList(childrenQuery);
            Map<Long, Node> childrenMap = children.stream().collect(Collectors.toMap(Node::getId, node -> node));
            if (childrenMap.containsKey(args.getParentId())) throw new BusinessException("不能设置自己的子节点为父节点");
        }

        // 校验名称是否重复（如果名称有变更）
        if (!args.getName().equals(existingNode.getNodeName())) {
            Long parentId = Objects.equals(args.getParentId(), existingNode.getParentId())
                    ? existingNode.getParentId() : args.getParentId();
            LambdaQueryWrapper<Node> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Node::getParentId, parentId).eq(Node::getNodeName, args.getName()).ne(Node::getId, args.getId());
            if (nodeMapper.selectCount(queryWrapper) > 0) throw new BusinessException("同一父节点下已存在相同名称的节点");
        }

        // 更新组织节点
        LambdaUpdateWrapper<Node> nodeUpdate = new LambdaUpdateWrapper<>();
        nodeUpdate.set(Node::getNodeName, args.getName())
                .set(Node::getNodeType, args.getType())
                .set(Node::getParentId, args.getParentId())
                .set(Node::getIsEnabled, args.getIsEnabled())
                .eq(Node::getId, args.getId());
        if (nodeMapper.update(nodeUpdate) != 1) throw new BusinessException("更新节点失败");

        // 校验节点管理员是否存在
        Long adminId;
        if (args.getAdminUsername().isBlank()) {
            adminId = 0L;
        } else {
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.eq(User::getUsername, args.getAdminUsername());
            User admin = userMapper.selectOne(userQuery);
            if (admin == null) throw new BusinessException("该用户不存在");
            adminId = admin.getId();
        }

        // 更新
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getNodeId, args.getId());
        Drive drive = driveMapper.selectOne(driveQuery);
        if (drive == null) throw new BusinessException("节点对应的存储空间不存在");
        if (drive.getUsedQuota() > args.getStorageQuota()) throw new BusinessException("");

        LambdaUpdateWrapper<Drive> driveUpdate = new LambdaUpdateWrapper<>();
        driveUpdate.set(Drive::getDriveName, args.getName())
                .set(Drive::getTotalQuota, args.getStorageQuota())
                .set(Drive::getUserId, adminId)
                .eq(Drive::getNodeId, drive.getNodeId());
        if (driveMapper.update(driveUpdate) != 1) throw new BusinessException("更新节点-空间失败");
    }

    public List<NodeView> listAllOrgNodes() {
        // 查询组织节点
        LambdaQueryWrapper<Node> nodeQuery = new LambdaQueryWrapper<>();
        nodeQuery.eq(Node::getIsDeleted, 0);
        List<Node> nodeList = nodeMapper.selectList(nodeQuery);
        if (nodeList == null || nodeList.isEmpty()) return List.of();
        Map<Long, String> parentNodeMap = nodeList.stream().collect(Collectors.toMap(Node::getId, Node::getNodeName));

        // 查询空间
        List<Long> nodeIdList = nodeList.stream().map(Node::getId).toList();
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.in(Drive::getNodeId, nodeIdList);
        List<Drive> driveList = driveMapper.selectList(driveQuery);
        Map<Long, Drive> driveMap = driveList.stream().collect(Collectors.toMap(Drive::getNodeId, drive -> drive));

        // 查询用户
        Set<Long> adminIdList = driveList.stream().map(Drive::getUserId).collect(Collectors.toSet());
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.in(User::getId, adminIdList);
        List<User> userList = userMapper.selectList(userQuery);
        Map<Long, String> userMap = userList.stream().collect(Collectors.toMap(User::getId, User::getUsername));

        List<NodeView> nodeViewList = nodeList.stream()
                .map(node -> {
                    String parentName = parentNodeMap.getOrDefault(node.getParentId(), "根节点");
                    Drive drive = driveMap.get(node.getId());
                    Long storageQuota = drive.getTotalQuota();
                    String adminUsername = userMap.getOrDefault(drive.getUserId(), "");

                    return NodeView.builder()
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

        return nodeViewList;
    }
}
