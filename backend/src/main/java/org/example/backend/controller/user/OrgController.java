package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.model.response.org.OrgNodeTreeResp;
import org.example.backend.service.OrgService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tree")
public class OrgController {
    private final OrgService orgService;

    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }

    @GetMapping("/org")
    public Result<?> getOrgTree() {
        List<OrgNodeTreeResp> resp = orgService.getOrgTree();
        return Result.success(resp);
    }
}
