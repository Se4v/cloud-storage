package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.request.CreateMemberArgs;
import org.example.backend.model.request.DeleteMemberArgs;
import org.example.backend.model.request.UpdateMemberArgs;
import org.example.backend.model.entity.Node;
import org.example.backend.model.entity.Role;
import org.example.backend.model.response.MemberNodeView;
import org.example.backend.model.response.MemberRoleView;
import org.example.backend.model.response.MemberView;
import org.example.backend.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
public class MemberManageController {
    @Autowired
    private MemberService memberService;

    @PostMapping("/create")
    public Result<Void> createMember(@RequestBody CreateMemberArgs args) {
        memberService.createMember(args);
        return Result.success("");
    }

    @PostMapping("/delete")
    public Result<Void> deleteMembers(@RequestBody DeleteMemberArgs args) {
        memberService.deleteMembers(args);
        return Result.success("");
    }

    @PostMapping("/update")
    public Result<Void> updateMember(@RequestBody UpdateMemberArgs args) {
        memberService.updateMember(args);
        return Result.success("");
    }

    @GetMapping("/all")
    public Result<List<MemberView>> listAllMembers() {
        List<MemberView> memberViews = memberService.listAllMembers();
        return Result.success(memberViews);
    }

    @GetMapping("/role")
    public Result<List<MemberRoleView>> listOrgRoles() {
        List<Role> roles = memberService.listOrgRoles();
        List<MemberRoleView> memberRoleViews = roles.stream()
                .map(role -> MemberRoleView.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build())
                .toList();
        return Result.success(memberRoleViews);
    }

    @GetMapping("/org")
    public Result<List<MemberNodeView>> listOrgNodes() {
        List<Node> nodes = memberService.listOrgNodes();
        List<MemberNodeView> memberNodeViews = nodes.stream()
                .map(node -> MemberNodeView.builder()
                        .id(node.getId())
                        .name(node.getNodeName())
                        .build())
                .toList();
        return Result.success(memberNodeViews);
    }
}
