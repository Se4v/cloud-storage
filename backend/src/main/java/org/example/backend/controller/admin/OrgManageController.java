package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.notice.NodeCreationReq;
import org.example.backend.model.request.org.OrgNodeDeletionReq;
import org.example.backend.model.request.org.OrgNodeUpdateReq;
import org.example.backend.model.response.org.OrgNodeResp;
import org.example.backend.service.OrgService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org")
public class OrgManageController {
    private final OrgService orgService;

    public OrgManageController(OrgService orgService) {
        this.orgService = orgService;
    }

    /**
     * 创建组织节点
     * @param req 节点创建请求参数
     * @return 统一响应结果
     */
    @PostMapping("/create")
    public Result<?> createOrgNode(@RequestBody NodeCreationReq req) {
        orgService.createOrgNode(req);
        return Result.success();
    }

    /**
     * 批量删除组织节点
     * @param req 节点删除请求参数（支持批量）
     * @return 统一响应结果
     */
    @PostMapping("/delete")
    public Result<?> deleteOrgNodes(@RequestBody OrgNodeDeletionReq req) {
        orgService.deleteOrgNodes(req);
        return Result.success();
    }

    /**
     * 更新组织节点信息
     * @param req 节点更新请求参数
     * @return 统一响应结果
     */
    @PostMapping("/update")
    public Result<?> updateOrgNode(@RequestBody OrgNodeUpdateReq req) {
        orgService.updateOrgNode(req);
        return Result.success();
    }

    /**
     * 查询所有组织部门节点列表
     * @return 组织部门节点列表数据
     */
    @GetMapping("/all")
    public Result<?> listAllOrgNodes() {
        List<OrgNodeResp> resp = orgService.listAllOrgNodes();
        return Result.success(resp);
    }
}
