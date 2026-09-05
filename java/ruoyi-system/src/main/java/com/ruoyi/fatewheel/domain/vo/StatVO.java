package com.ruoyi.fatewheel.domain.vo;

import java.io.Serializable;
import java.util.Map;

/**
 * App 端统计视图对象
 */
public class StatVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 我的标记总数 */
    private Integer totalMark;

    /** 已去过数量 */
    private Integer visitedCount;

    /** 本月决策次数 */
    private Integer monthDecisions;

    /** 连续决策天数 */
    private Integer streak;

    /** 小组数 */
    private Integer groupCount;

    /** 拔草率（百分比整数） */
    private Integer weedRate;

    /** 决策类型统计（select/go/buy 次数） */
    private Map<String, Integer> decisionTypes;

    public Integer getTotalMark() { return totalMark; }
    public void setTotalMark(Integer totalMark) { this.totalMark = totalMark; }
    public Integer getVisitedCount() { return visitedCount; }
    public void setVisitedCount(Integer visitedCount) { this.visitedCount = visitedCount; }
    public Integer getMonthDecisions() { return monthDecisions; }
    public void setMonthDecisions(Integer monthDecisions) { this.monthDecisions = monthDecisions; }
    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }
    public Integer getGroupCount() { return groupCount; }
    public void setGroupCount(Integer groupCount) { this.groupCount = groupCount; }
    public Integer getWeedRate() { return weedRate; }
    public void setWeedRate(Integer weedRate) { this.weedRate = weedRate; }
    public Map<String, Integer> getDecisionTypes() { return decisionTypes; }
    public void setDecisionTypes(Map<String, Integer> decisionTypes) { this.decisionTypes = decisionTypes; }
}
