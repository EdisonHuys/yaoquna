package com.ruoyi.fatewheel.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;

/**
 * 收藏 Mapper
 */
public interface FwFavoriteMapper
{
    @Select("select count(*) from fw_favorite where user_id = #{userId} and mark_id = #{markId}")
    public int countFavorite(@Param("userId") Long userId, @Param("markId") Long markId);

    @Insert("insert into fw_favorite(user_id, mark_id, create_time) values(#{userId}, #{markId}, sysdate())")
    public int insertFavorite(@Param("userId") Long userId, @Param("markId") Long markId);

    @Delete("delete from fw_favorite where user_id = #{userId} and mark_id = #{markId}")
    public int deleteFavorite(@Param("userId") Long userId, @Param("markId") Long markId);

    @Select("select count(*) from fw_favorite where mark_id = #{markId}")
    public int countByMark(@Param("markId") Long markId);
}
