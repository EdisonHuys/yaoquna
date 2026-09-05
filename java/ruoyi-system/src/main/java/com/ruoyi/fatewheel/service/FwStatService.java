package com.ruoyi.fatewheel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.fatewheel.domain.FwUserExtend;
import com.ruoyi.fatewheel.domain.vo.FwMarkVO;
import com.ruoyi.fatewheel.domain.vo.StatVO;
import com.ruoyi.fatewheel.mapper.FwDecisionMapper;
import com.ruoyi.fatewheel.mapper.FwGroupMapper;
import com.ruoyi.fatewheel.mapper.FwMarkMapper;
import com.ruoyi.fatewheel.mapper.FwOrderMapper;
import com.ruoyi.fatewheel.mapper.FwUserExtendMapper;

/**
 * 统计业务：App 端个人统计 + 管理端全局统计
 */
@Service
public class FwStatService
{
    @Autowired
    private FwMarkMapper markMapper;

    @Autowired
    private FwDecisionMapper decisionMapper;

    @Autowired
    private FwGroupMapper groupMapper;

    @Autowired
    private FwUserExtendMapper userExtendMapper;

    @Autowired
    private FwOrderMapper orderMapper;

    /** App 端个人统计 */
    public StatVO personalStats(Long userId)
    {
        StatVO vo = new StatVO();
        // 我的标记
        com.ruoyi.fatewheel.domain.FwMark q = new com.ruoyi.fatewheel.domain.FwMark();
        q.setUserId(userId);
        List<FwMarkVO> myMarks = markMapper.selectFwMarkList(q);
        int total = 0;
        int visited = 0;
        for (FwMarkVO m : myMarks)
        {
            total++;
            if ("visited".equals(m.getStatus()))
            {
                visited++;
            }
        }
        vo.setTotalMark(total);
        vo.setVisitedCount(visited);
        vo.setWeedRate(total == 0 ? 0 : Math.round(visited * 100.0f / total));

        // 决策统计
        long month = decisionMapper.countByUserThisMonth(userId);
        vo.setMonthDecisions((int) month);
        long all = decisionMapper.countByUser(userId);

        // streak（简化：统计全部决策天数作为连签天数，够用即可）
        FwUserExtend ext = userExtendMapper.selectByUserId(userId);
        int streak = (ext != null && ext.getStreak() != null) ? ext.getStreak() : (int) all;
        vo.setStreak(streak);

        // 小组数
        vo.setGroupCount(groupMapper.selectMyGroups(userId).size());

        // 决策类型分布
        Map<String, Integer> types = new HashMap<>();
        types.put("select", 0);
        types.put("go", 0);
        types.put("buy", 0);
        for (Map<String, Object> row : decisionMapper.countByType(userId))
        {
            Object type = row.get("decision_type");
            Object cnt = row.get("cnt");
            if (type != null && cnt != null)
            {
                types.put(String.valueOf(type), ((Number) cnt).intValue());
            }
        }
        vo.setDecisionTypes(types);
        return vo;
    }

    /** 管理端全局统计 */
    public Map<String, Object> globalStats()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("markCount", markMapper.countAll());
        data.put("markByCategory", markMapper.countByCategory());
        data.put("paidOrderCount", orderMapper.countPaid());
        return data;
    }
}
