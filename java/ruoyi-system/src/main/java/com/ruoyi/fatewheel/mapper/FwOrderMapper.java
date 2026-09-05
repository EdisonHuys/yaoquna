package com.ruoyi.fatewheel.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import com.ruoyi.fatewheel.domain.FwOrder;

/**
 * 权益订单 Mapper
 */
public interface FwOrderMapper
{
    @Select("select * from fw_order where order_id = #{orderId}")
    public FwOrder selectFwOrderById(Long orderId);

    @Select("select * from fw_order where order_no = #{orderNo}")
    public FwOrder selectFwOrderByNo(String orderNo);

    @Select("<script>"
        + "select * from fw_order "
        + "where 1=1 "
        + "<if test='userId != null'> and user_id = #{userId} </if>"
        + "<if test='itemType != null and itemType != \"\"'> and item_type = #{itemType} </if>"
        + "<if test='status != null and status != \"\"'> and status = #{status} </if>"
        + "order by create_time desc"
        + "</script>")
    public List<FwOrder> selectFwOrderList(FwOrder query);

    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    @Insert("insert into fw_order(order_no, user_id, item_type, amount, status, create_time) "
        + "values(#{orderNo}, #{userId}, #{itemType}, #{amount}, 'pending', sysdate())")
    public int insertFwOrder(FwOrder order);

    @Update("update fw_order set status = #{status}, pay_time = sysdate() where order_id = #{orderId}")
    public int updateFwOrder(FwOrder order);

    @Select("select count(*) from fw_order where status = 'paid'")
    public long countPaid();
}
