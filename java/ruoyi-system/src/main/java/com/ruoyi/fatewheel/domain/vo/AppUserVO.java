package com.ruoyi.fatewheel.domain.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * App 端用户信息视图对象
 */
public class AppUserVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** sys_user.user_id */
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 学校id */
    private Long schoolId;

    /** 学校名称 */
    private String schoolName;

    /** 连续决策天数 */
    private Integer streak;

    /** 是否Pro（0否 1是） */
    private String isPro;

    /** 共享地点数 */
    private Integer sharedCount;

    /** 被收藏总数 */
    private Integer totalFavorites;

    /** 加入的小组 */
    private List<Map<String, Object>> groups;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }
    public String getIsPro() { return isPro; }
    public void setIsPro(String isPro) { this.isPro = isPro; }
    public Integer getSharedCount() { return sharedCount; }
    public void setSharedCount(Integer sharedCount) { this.sharedCount = sharedCount; }
    public Integer getTotalFavorites() { return totalFavorites; }
    public void setTotalFavorites(Integer totalFavorites) { this.totalFavorites = totalFavorites; }
    public List<Map<String, Object>> getGroups() { return groups; }
    public void setGroups(List<Map<String, Object>> groups) { this.groups = groups; }
}
