package com.ruoyi.fatewheel.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 举报 fw_report
 */
public class FwReport implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 举报id */
    private Long reportId;

    /** 举报人user_id */
    private Long userId;

    /** 被举报标记id */
    private Long markId;

    /** 举报原因 */
    private String reason;

    /** 状态（pending待处理/handled已处理/rejected驳回） */
    private String status;

    /** 处理人 */
    private Long handleBy;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 处理备注 */
    private String handleRemark;

    /** 举报时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getMarkId() { return markId; }
    public void setMarkId(Long markId) { this.markId = markId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getHandleBy() { return handleBy; }
    public void setHandleBy(Long handleBy) { this.handleBy = handleBy; }

    public Date getHandleTime() { return handleTime; }
    public void setHandleTime(Date handleTime) { this.handleTime = handleTime; }

    public String getHandleRemark() { return handleRemark; }
    public void setHandleRemark(String handleRemark) { this.handleRemark = handleRemark; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
