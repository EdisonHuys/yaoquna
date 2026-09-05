package com.ruoyi.fatewheel.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 决策记录 fw_decision
 */
public class FwDecision implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 决策id */
    private Long decisionId;

    /** 用户id */
    private Long userId;

    /** 类型（select选什么/go去不去/buy买不买） */
    private String decisionType;

    /** 决策主题 */
    private String title;

    /** 决策结果 */
    private String result;

    /** 决策详情/备注 */
    private String detail;

    /** 是否已执行（0否 1是） */
    private String executed;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getDecisionId() { return decisionId; }
    public void setDecisionId(Long decisionId) { this.decisionId = decisionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDecisionType() { return decisionType; }
    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getExecuted() { return executed; }
    public void setExecuted(String executed) { this.executed = executed; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
