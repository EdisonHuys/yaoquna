package com.ruoyi.fatewheel.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 用户扩展 fw_user_extend
 */
public class FwUserExtend implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 扩展id */
    private Long extendId;

    /** 关联sys_user.user_id */
    private Long userId;

    /** 微信openid */
    private String openid;

    /** 学校id */
    private Long schoolId;

    /** 连续决策天数 */
    private Integer streak;

    /** 是否Pro会员（0否 1是） */
    private String isPro;

    /** Pro到期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date proExpireTime;

    /** 共享地点数 */
    private Integer sharedCount;

    /** 被收藏总数 */
    private Integer totalFavorites;

    /** 小组邀请码 */
    private String inviteCode;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Long getExtendId() { return extendId; }
    public void setExtendId(Long extendId) { this.extendId = extendId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }

    public String getIsPro() { return isPro; }
    public void setIsPro(String isPro) { this.isPro = isPro; }

    public Date getProExpireTime() { return proExpireTime; }
    public void setProExpireTime(Date proExpireTime) { this.proExpireTime = proExpireTime; }

    public Integer getSharedCount() { return sharedCount; }
    public void setSharedCount(Integer sharedCount) { this.sharedCount = sharedCount; }

    public Integer getTotalFavorites() { return totalFavorites; }
    public void setTotalFavorites(Integer totalFavorites) { this.totalFavorites = totalFavorites; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
