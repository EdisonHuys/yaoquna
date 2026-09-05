package com.ruoyi.fatewheel.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 地点标记 fw_mark
 */
public class FwMark implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 标记id */
    private Long markId;

    /** 关键字（查询条件，非表字段：按名称/地址模糊搜索） */
    private String keyword;

    /** 创建者user_id */
    private Long userId;

    /** 地点名称 */
    private String name;

    /** 分类（美食/玩乐/购物/自习/其他） */
    private String category;

    /** 纬度 */
    private BigDecimal lat;

    /** 经度 */
    private BigDecimal lng;

    /** 地址 */
    private String address;

    /** 备注小贴士 */
    private String remark;

    /** 评分1-5 */
    private Integer rating;

    /** 人均/价格 */
    private BigDecimal price;

    /** 标签（逗号分隔） */
    private String tags;

    /** 图片url（逗号分隔） */
    private String images;

    /** 共享范围（private/group/school） */
    private String shareScope;

    /** 共享的小组id（逗号分隔） */
    private String groupIds;

    /** 所属学校id */
    private Long schoolId;

    /** 状态（normal/visited/off） */
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 被收藏次数 */
    private Integer favoriteCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Long getMarkId() { return markId; }
    public void setMarkId(Long markId) { this.markId = markId; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }

    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getShareScope() { return shareScope; }
    public void setShareScope(String shareScope) { this.shareScope = shareScope; }

    public String getGroupIds() { return groupIds; }
    public void setGroupIds(String groupIds) { this.groupIds = groupIds; }

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
