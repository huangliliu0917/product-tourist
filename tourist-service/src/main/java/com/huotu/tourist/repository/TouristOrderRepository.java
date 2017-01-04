package com.huotu.tourist.repository;

import com.huotu.tourist.common.OrderStateEnum;
import com.huotu.tourist.entity.TouristGood;
import com.huotu.tourist.entity.TouristOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 线路订单持久层
 * Created by slt on 2016/12/19.
 */
public interface TouristOrderRepository extends JpaRepository<TouristOrder, Long>,
        JpaSpecificationExecutor<TouristOrder> {

    @Query("update TouristOrder as o set o.remarks=?1 where o.id=?2")
    @Modifying
    @Transactional
    int modifyRemarks(String remark, Long id);

    @Query("update TouristOrder as o set o.orderState=?1 where o.id=?2")
    @Modifying
    @Transactional
    int modifyOrderState(OrderStateEnum orderState, Long id);

    Long countByTouristGood(TouristGood good);

    @Query("select sum(t.orderMoney) as om from TouristOrder as t where t.touristGood=?1")
    BigDecimal countOrderMoney(TouristGood good);

    /**
     * 统计当前状态订单的金额
     *
     * @param supplierId
     * @param orderState
     * @return
     */
    @Query("select sum(t.orderMoney) as om from TouristOrder as t where t.touristGood.touristSupplier.id=?1 and t" +
            ".orderState=?2")
    BigDecimal sumRefundTotal(Long supplierId, OrderStateEnum orderState);

    /**
     * 统计供应商订单的总佣金
     *
     * @param supplierId
     * @return
     */
    @Query("select sum(t.orderMoney*t.touristGood.rebate) as om from TouristOrder as t where t.touristGood.touristSupplier.id=?1 ")
    BigDecimal sumCommissionTotal(Long supplierId);

    /**
     * 统计供应商所有订单的总金额
     *
     * @param supplierId
     * @return
     */
    @Query("select sum(t.orderMoney) as om from TouristOrder as t where t.touristGood.touristSupplier.id=?1 ")
    BigDecimal sumMoneyTotal(Long supplierId);


    long countByTouristGood_TouristSupplier_id(Long supplierId);
}
