package roomescape.controller.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.MemberResponse;
import roomescape.service.serviceimpl.MemberService;

import java.util.List;

@RestController
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public List<MemberResponse> members() {
        return MemberResponse.listOf(memberService.findAllMembers());
    }
}
