package com.ruoyi.fatewheel.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fatewheel.domain.FwOrder;
import com.ruoyi.fatewheel.mapper.FwOrderMapper;

/**
 * 权益订单业务（App 端个人虚拟支付对接）
 * 商品类型：pro会员 / unlimited无限标记 / skin转盘皮肤 / adfree去广告
 */
@Service
public class FwOrderService
{
    @Autowired
    private FwOrderMapper orderMapper;

    private static final Map<String, BigDecimal> PRICES = new java.util.HashMap<String, BigDecimal>()
    {
        {
            put("pro", new BigDecimal("25"));
            put("unlimited", new BigDecimal("6"));
            put("skin", new BigDecimal("4"));
            put("adfree", new BigDecimal("7"));
        }
    };

    /** App 端：创建订单（返回待支付订单，接支付后回调确认） */
    public FwOrder createOrder(Long userId, String itemType)
    {
        BigDecimal price = PRICES.get(itemType);
        if (price == null)
        {
            throw new ServiceException("不支持的权益类型");
        }
        FwOrder order = new FwOrder();
        order.setOrderNo(genOrderNo());
        order.setUserId(userId);
        order.setItemType(itemType);
        order.setAmount(price);
        order.setStatus("pending");
        orderMapper.insertFwOrder(order);
        return order;
    }

    /** 支付成功确认（支付平台回调或 App 通知后调用，发放权益） */
    public void confirmPay(Long orderId)
    {
        FwOrder order = orderMapper.selectFwOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if ("paid".equals(order.getStatus()))
        {
            return;
        }
        order.setStatus("paid");
        orderMapper.updateFwOrder(order);
        // TODO: 发放权益（is_pro=1 或 解锁标记额度等），可在此扩展
    }

    /** 我的订单（App） */
    public List<FwOrder> selectMyOrders(Long userId)
    {
        FwOrder q = new FwOrder();
        q.setUserId(userId);
        return orderMapper.selectFwOrderList(q);
    }

    /** 管理端：订单列表 */
    public List<FwOrder> selectOrderList(FwOrder query)
    {
        return orderMapper.selectFwOrderList(query);
    }

    public long countPaid()
    {
        return orderMapper.countPaid();
    }

    private String genOrderNo()
    {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
            + String.format("%06d", (int) (Math.random() * 1000000));
    }
}
