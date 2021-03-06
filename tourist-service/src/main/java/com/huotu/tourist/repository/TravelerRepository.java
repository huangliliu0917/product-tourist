package com.huotu.tourist.repository;

import com.huotu.tourist.common.OrderStateEnum;
import com.huotu.tourist.entity.TouristGood;
import com.huotu.tourist.entity.TouristRoute;
import com.huotu.tourist.entity.Traveler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 游客持久层
 * Created by slt on 2016/12/19.
 */
@Repository
public interface TravelerRepository extends JpaRepository<Traveler, Long>, JpaSpecificationExecutor<Traveler> {


    List<Traveler> findByRoute_Id(Long routeId);

    @Query("update Traveler as t set t.route=?1 where t.route=?2 ")
    @Modifying
    @Transactional
    int modifyRouteIdByRouteId(TouristRoute later,TouristRoute former);

    List<Traveler> findByOrder_Id(Long orderId);

    Long countByOrder_Id(Long orderId);

    Long countByOrder_TouristGood(TouristGood touristGood);

    int countByRoute(TouristRoute route);

    @Query("delete from Traveler as t where t.order.orderState = ?1 and t.order.createTime<?2 ")
    @Modifying
    int scheduledCancelOrder(OrderStateEnum orderState, LocalDateTime now);
}
