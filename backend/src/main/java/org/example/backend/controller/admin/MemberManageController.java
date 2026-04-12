package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.member.MemberCreationReq;
import org.example.backend.model.request.member.MemberDeletionReq;
import org.example.backend.model.request.member.MemberUpdateReq;
import org.example.backend.model.entity.Node;
import org.example.backend.model.entity.Role;
import org.example.backend.model.response.member.MemberNodeView;
import org.example.backend.model.response.member.MemberRoleView;
import org.example.backend.model.response.member.MemberView;
import org.example.backend.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
public class MemberManageController {
    private final MemberService memberService;

    public MemberManageController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/create")
    public Result<?> createMember(@RequestBody MemberCreationReq req) {
        memberService.createMember(req);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteMembers(@RequestBody MemberDeletionReq req) {
        memberService.deleteMembers(req);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<?> updateMember(@RequestBody MemberUpdateReq req) {
        memberService.updateMember(req);
        return Result.success();
    }

    @GetMapping("/all")
    public Result<?> listAllMembers() {
        List<MemberView> resp = memberService.listAllMembers();
        return Result.success(resp);
    }

    @GetMapping("/role")
    public Result<?> listOrgRoles() {
        List<Role> roles = memberService.listOrgRoles();
        List<MemberRoleView> resp = roles.stream()
                .map(role -> MemberRoleView.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build())
                .toList();
        return Result.success(resp);
    }

    @GetMapping("/org")
    public Result<?> listOrgNodes() {
        List<Node> nodes = memberService.listOrgNodes();
        List<MemberNodeView> resp = nodes.stream()
                .map(node -> MemberNodeView.builder()
                        .id(node.getId())
                        .name(node.getNodeName())
                        .build())
                .toList();
        return Result.success(resp);
    }
}
