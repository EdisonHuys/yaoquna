package com.ruoyi.fatewheel.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 地点标记视图对象（含创建者昵称、是否已收藏等展示字段）
 */
public class FwMarkVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long markId;
    private Long userId;
    private String name;
    private String category;
    private BigDecimal lat;
    private BigDecimal lng;
    private String address;
    private String remark;
    private Integer rating;
    private BigDecimal price;
    private String tags;
    private String images;
    private String shareScope;
    private String groupIds;
    private Long schoolId;
    private String status;
    private Integer favoriteCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 创建者昵称 */
    private String creatorName;
    /** 创建者头像 */
    private String creatorAvatar;
    /** 学校名称 */
    private String schoolName;
    /** 当前用户是否已收藏（0否 1是） */
    private Integer favorited;

    public Long getMarkId() { return markId; }
    public void setMarkId(Long markId) { this.markId = markId; }
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
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getCreatorAvatar() { return creatorAvatar; }
    public void setCreatorAvatar(String creatorAvatar) { this.creatorAvatar = creatorAvatar; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public Integer getFavorited() { return favorited; }
    public void setFavorited(Integer favorited) { this.favorited = favorited; }
}
