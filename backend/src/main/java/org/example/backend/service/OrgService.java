package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.MemberMapper;
import org.example.backend.mapper.NodeMapper;
import org.example.backend.model.args.CreateNodeArgs;
import org.example.backend.model.args.UpdateNodeArgs;
import org.example.backend.model.entity.Member;
import org.example.backend.model.entity.Node;
import org.example.backend.model.result.NodeDetailResult;
import org.example.backend.model.view.NodeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrgService {
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private NodeMapper nodeMapper;

    private static final int DELETED = 1;

    public List<NodeView> getOrgTree(Long userId) {
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Member::getUserId, userId);
        List<Member> members = memberMapper.selectList(queryWrapper);

        List<Long> nodeIds = members.stream().map(Member::getNodeId).toList();

        List<Node> nodeList = nodeMapper.selectNodeWithParents(nodeIds);

        List<NodeView> viewList = nodeList.stream()
                .map(node -> {
                    NodeView view = new NodeView();

                    view.setId(String.valueOf(node.getId()));
                    view.setLabel(node.getNodeName());
                    view.setParentId(node.getParentId());

                    switch (node.getNodeType()) {
                        case 1: view.setType("company"); break;
                        case 2: view.setType("dept"); break;
                        case 3: view.setType("team"); break;
                        default: view.setType("unknown"); break;
                    }

                    return view;
                })
                .toList();

        return buildTree(viewList);
    }

    private List<NodeView> buildTree(List<NodeView> flatList) {
        if (flatList == null || flatList.isEmpty()) {
            return List.of();
        }

        // 建立 ID -> 节点的映射表，用于快速查找
        Map<String, NodeView> nodeMap = flatList.stream()
                .collect(Collectors.toMap(NodeView::getId, node -> node));

        // 2. 组装父子关系
        List<NodeView> roots = new ArrayList<>();

        for (NodeView node : flatList) {
            if (node.getParentId() == null || node.getParentId() == 0L) {
                // 根节点（parent_id = 0）
                roots.add(node);
            } else {
                // 挂载到父节点下
                NodeView parent = nodeMap.get(String.valueOf(node.getParentId()));
                if (parent != null) parent.getChildren().add(node);
                    // 父节点不存在（数据异常），作为根节点处理
                else roots.add(node);
            }
        }

        return roots;
    }

    /**
     * 创建部门节点
     */
    @Transactional
    public void createNode(CreateNodeArgs args, Long userId) {
        // 校验父节点是否存在（如果不是根节点）
        if (args.getParentId() != null && args.getParentId() > 0) {
            Node parentNode = nodeMapper.selectById(args.getParentId());
            if (parentNode == null) {
                throw new BusinessException("父节点不存在");
            }
        }

        // 校验同一父节点下名称是否重复
        LambdaQueryWrapper<Node> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Node::getParentId, args.getParentId())
                .eq(Node::getNodeName, args.getName());
        if (nodeMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("同一父节点下已存在相同名称的部门");
        }

        // 创建节点
        Node node = Node.builder()
                .nodeName(args.getName())
                .nodeType(args.getType())
                .parentId(args.getParentId())
                .enabled(1)
                .createdAt(LocalDateTime.now())
                .creatorId(userId)
                .updatedAt(LocalDateTime.now())
                .updaterId(userId)
                .build();

        int count = nodeMapper.insert(node);
        if (count != 1) {
            throw new BusinessException("创建部门失败");
        }
    }

    /**
     * 批量删除部门节点
     */
    @Transactional
    public void deleteNodes(List<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return;
        }

        // 校验是否存在子节点
        for (Long nodeId : nodeIds) {
            LambdaQueryWrapper<Node> childQuery = new LambdaQueryWrapper<>();
            childQuery.eq(Node::getParentId, nodeId);
            if (nodeMapper.selectCount(childQuery) > 0) {
                throw new BusinessException("请先删除该部门下的所有子部门");
            }
        }

        // 删除节点
        LambdaUpdateWrapper<Node> nodeWrapper = new LambdaUpdateWrapper<>();
        nodeWrapper.set(Node::getDeleted, DELETED)
                .in(Node::getId, nodeIds);
        if (nodeMapper.update(nodeWrapper) != nodeIds.size()) {
            throw new BusinessException("删除部门失败");
        }

        // 删除相关成员关联
        LambdaUpdateWrapper<Member> memberWrapper = new LambdaUpdateWrapper<>();
        memberWrapper.set(Member::getDeleted, DELETED)
                .in(Member::getNodeId, nodeIds);
        if (memberMapper.update(memberWrapper) != nodeIds.size()) {
            throw new BusinessException("删除部门失");
        }
    }

    @Transactional
    public void updateNode(UpdateNodeArgs args, Long userId) {
        if (args.getId() == null) {
            throw new BusinessException("节点ID不能为空");
        }

        // 校验节点是否存在
        Node existingNode = nodeMapper.selectById(args.getId());
        if (existingNode == null) {
            throw new BusinessException("部门不存在");
        }

        // 校验父节点是否合法（不能设置自己或自己的子节点为父节点）
        if (args.getParentId() != null && args.getParentId() > 0) {
            if (args.getParentId().equals(args.getId())) {
                throw new BusinessException("不能将部门设置为自己的父部门");
            }
            Node parentNode = nodeMapper.selectById(args.getParentId());
            if (parentNode == null) {
                throw new BusinessException("父部门不存在");
            }
            // TODO: 检查是否将父节点设置为自己的子节点（避免循环依赖）
        }

        // 校验名称是否重复（如果名称有变更）
        if (args.getName() != null && !args.getName().equals(existingNode.getNodeName())) {
            Long parentId = args.getParentId() != null ? args.getParentId() : existingNode.getParentId();
            LambdaQueryWrapper<Node> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Node::getParentId, parentId)
                    .eq(Node::getNodeName, args.getName())
                    .ne(Node::getId, args.getId());
            if (nodeMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("同一父部门下已存在相同名称的部门");
            }
        }

        // 构建更新条件
        LambdaUpdateWrapper<Node> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(args.getName() != null, Node::getNodeName, args.getName())
                .set(args.getType() != null, Node::getNodeType, args.getType())
                .set(args.getParentId() != null, Node::getParentId, args.getParentId())
                .set(args.getEnabled() != null, Node::getEnabled, args.getEnabled())
                .set(Node::getUpdaterId, userId)
                .set(Node::getUpdatedAt, LocalDateTime.now())
                .eq(Node::getId, args.getId());

        int count = nodeMapper.update(updateWrapper);
        if (count != 1) {
            throw new BusinessException("更新部门失败");
        }
    }

    public List<NodeDetailResult> listAllNodes() {
        return nodeMapper.selectNode();
    }
}
