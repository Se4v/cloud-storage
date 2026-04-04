package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.model.view.OrgTreeView;
import org.example.backend.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tree")
public class OrgController {
    @Autowired
    private OrgService orgService;

    Long userId = 2034965772877197313L;

    @GetMapping("/org")
    public Result<?> getOrgTree() {
        List<OrgTreeView> views = orgService.getOrgTree(userId);
        return Result.success(views);
    }
}
