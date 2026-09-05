package com.ruoyi.fatewheel.app;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.fatewheel.domain.FwDecision;
import com.ruoyi.fatewheel.domain.FwMark;
import com.ruoyi.fatewheel.domain.FwReport;
import com.ruoyi.fatewheel.domain.FwSchool;
import com.ruoyi.fatewheel.domain.vo.FwMarkVO;
import com.ruoyi.fatewheel.domain.vo.StatVO;
import com.ruoyi.fatewheel.service.FwDecisionService;
import com.ruoyi.fatewheel.service.FwGroupService;
import com.ruoyi.fatewheel.service.FwMarkService;
import com.ruoyi.fatewheel.service.FwOrderService;
import com.ruoyi.fatewheel.service.FwReportService;
import com.ruoyi.fatewheel.service.FwSchoolService;
import com.ruoyi.fatewheel.service.FwStatService;
import com.ruoyi.fatewheel.service.FwUserService;

/**
 * App 端 API（微信小程序 / uni-app 打包 App 共用）
 * 除 /api/login 外均需携带 Authorization: Bearer token
 */
@RestController
@RequestMapping("/api")
public class FateAppController extends BaseController
{
    @Autowired
    private FwUserService userService;

    @Autowired
    private FwMarkService markService;

    @Autowired
    private FwGroupService groupService;

    @Autowired
    private FwDecisionService decisionService;

    @Autowired
    private FwSchoolService schoolService;

    @Autowired
    private FwReportService reportService;

    @Autowired
    private FwOrderService orderService;

    @Autowired
    private FwStatService statService;

    // ===== 用户 =====

    /**
     * 微信授权登录
     * body: { openid: string, nickname?: string, avatar?: string }
     * 说明：后端未配置微信 appid/secret 时，前端可传 uni.login 得到的 code 作为 openid（演示模式）
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> body)
    {
        String openid = body.getOrDefault("openid", body.getOrDefault("code", ""));
        String nickname = body.getOrDefault("nickname", "");
        String avatar = body.getOrDefault("avatar", "");
        Map<String, Object> result = userService.login(openid, nickname, avatar);
        return success(result);
    }

    @GetMapping("/profile")
    public AjaxResult profile()
    {
        return success(userService.getProfile(getUserId()));
    }

    @PutMapping("/profile/school")
    public AjaxResult setSchool(@RequestBody Map<String, Long> body)
    {
        userService.setSchool(getUserId(), body.get("schoolId"));
        return success();
    }

    // ===== 标记 =====

    @GetMapping("/marks")
    public AjaxResult marks(@RequestParam(value = "scope", required = false) String scope,
                            @RequestParam(value = "category", required = false) String category)
    {
        return success(markService.selectVisibleMarks(getUserId(), category, scope));
    }

    @GetMapping("/marks/{id}")
    public AjaxResult mark(@PathVariable Long id)
    {
        FwMarkVO vo = markService.selectFwMarkById(id);
        if (vo == null)
        {
            return error("标记不存在");
        }
        return success(vo);
    }

    @PostMapping("/marks")
    public AjaxResult createMark(@RequestBody FwMark mark)
    {
        return success(markService.insertFwMark(mark, getUserId()));
    }

    @PutMapping("/marks/{id}")
    public AjaxResult updateMark(@PathVariable Long id, @RequestBody FwMark mark)
    {
        mark.setMarkId(id);
        markService.updateFwMark(mark);
        return success();
    }

    @DeleteMapping("/marks/{id}")
    public AjaxResult deleteMark(@PathVariable Long id)
    {
        markService.deleteFwMarkById(id);
        return success();
    }

    /** 转盘素材池（当前用户可见的全部标记） */
    @GetMapping("/wheel/pool")
    public AjaxResult wheelPool(@RequestParam(value = "category", required = false) String category)
    {
        return success(markService.selectVisibleMarks(getUserId(), category, null));
    }

    /** 收藏 / 取消收藏 */
    @PostMapping("/marks/{id}/favorite")
    public AjaxResult favorite(@PathVariable Long id)
    {
        boolean now = markService.toggleFavorite(getUserId(), id);
        return success(now ? 1 : 0);
    }

    /** 标记已去 */
    @PostMapping("/marks/{id}/visited")
    public AjaxResult visited(@PathVariable Long id)
    {
        markService.markVisited(id);
        return success();
    }

    // ===== 决策 =====

    @GetMapping("/decisions")
    public AjaxResult decisions(@RequestParam(value = "type", required = false) String type)
    {
        return success(decisionService.selectDecisions(getUserId(), type));
    }

    @PostMapping("/decisions")
    public AjaxResult createDecision(@RequestBody FwDecision decision)
    {
        return success(decisionService.createDecision(decision, getUserId()));
    }

    // ===== 小组 =====

    @GetMapping("/groups")
    public AjaxResult groups()
    {
        return success(groupService.selectMyGroups(getUserId()));
    }

    @PostMapping("/groups")
    public AjaxResult createGroup(@RequestBody Map<String, String> body)
    {
        return success(groupService.createGroup(getUserId(), body.getOrDefault("groupName", "")));
    }

    @PostMapping("/groups/join")
    public AjaxResult joinGroup(@RequestBody Map<String, String> body)
    {
        return success(groupService.joinGroup(getUserId(), body.getOrDefault("inviteCode", "")));
    }

    @GetMapping("/groups/{id}/members")
    public AjaxResult groupMembers(@PathVariable Long id)
    {
        return success(groupService.selectMembers(id));
    }

    // ===== 学校 / 校友共享 =====

    @GetMapping("/schools")
    public AjaxResult schools()
    {
        FwSchool q = new FwSchool();
        q.setStatus("0");
        return success(schoolService.selectSchoolList(q));
    }

    @GetMapping("/alumni/marks")
    public AjaxResult alumniMarks(@RequestParam(value = "schoolId", required = false) Long schoolId,
                                  @RequestParam(value = "category", required = false) String category)
    {
        Long sid = schoolId;
        if (sid == null)
        {
            // 未指定时使用当前用户学校
            com.ruoyi.fatewheel.domain.vo.AppUserVO user = userService.getProfile(getUserId());
            sid = user.getSchoolId();
        }
        return success(markService.selectSchoolMarks(sid, getUserId(), category));
    }

    // ===== 举报 =====

    @PostMapping("/reports")
    public AjaxResult report(@RequestBody FwReport report)
    {
        return toAjax(reportService.submitReport(report, getUserId()));
    }

    // ===== 统计 =====

    @GetMapping("/stats")
    public AjaxResult stats()
    {
        StatVO vo = statService.personalStats(getUserId());
        return success(vo);
    }

    // ===== 权益订单 =====

    @PostMapping("/orders")
    public AjaxResult createOrder(@RequestBody Map<String, String> body)
    {
        return success(orderService.createOrder(getUserId(), body.getOrDefault("itemType", "")));
    }

    @GetMapping("/orders")
    public AjaxResult myOrders()
    {
        return success(orderService.selectMyOrders(getUserId()));
    }
}
