package com.huotu.tourist.service.impl;

import com.huotu.tourist.common.OrderStateEnum;
import com.huotu.tourist.common.PayTypeEnum;
import com.huotu.tourist.common.TravelerTypeEnum;
import com.huotu.tourist.converter.LocalDateTimeFormatter;
import com.huotu.tourist.entity.*;
import com.huotu.tourist.login.SystemUser;
import com.huotu.tourist.model.OrderStateQuery;
import com.huotu.tourist.repository.TouristGoodRepository;
import com.huotu.tourist.repository.TouristOrderRepository;
import com.huotu.tourist.repository.TouristRouteRepository;
import com.huotu.tourist.repository.TravelerRepository;
import com.huotu.tourist.service.ConnectMallService;
import com.huotu.tourist.service.TouristOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Created by lhx on 2017/1/3.
 */
@Service
public class TouristOrderServiceImpl implements TouristOrderService {
    @Autowired
    TouristOrderRepository touristOrderRepository;
    @Autowired
    TouristRouteRepository touristRouteRepository;
    @Autowired
    TravelerRepository travelerRepository;
    @Autowired
    TouristGoodRepository touristGoodRepository;
    @Autowired
    ConnectMallService connectMallService;


    @Override
    public URL startOrder(TouristGood good, Consumer<TouristOrder> success, Consumer<String> failed) {
        // TODO: 2017/1/3  
        return null;
    }

    @Override
    public Page<TouristOrder> touristOrders(TouristSupplier supplier, String supplierName, String orderNo
            , String touristName, String buyerName, String tel, PayTypeEnum payTypeEnum, LocalDateTime orderDate
            , LocalDateTime endOrderDate, LocalDateTime payDate, LocalDateTime endPayDate, LocalDateTime touristDate
            , LocalDateTime endTouristDate, OrderStateEnum orderStateEnum, Boolean settlement
            , Pageable pageable) throws IOException {
        return touristOrderRepository.findAll((root, query, cb) -> {
            Predicate predicate = cb.isTrue(cb.literal(true));
            if(settlement!=null){
                predicate=cb.and(predicate,cb.equal(root.get("settlement").as(Boolean.class),settlement));
            }
            if (supplier != null) {
                predicate = cb.and(predicate,cb.equal(root.get("touristGood").get("touristSupplier").as(TouristSupplier.class),
                        supplier));
            }
            if (!StringUtils.isEmpty(supplierName)) {
                predicate = cb.and(predicate, cb.like(root.get("touristGood").get("touristSupplier").get("supplierName").as(String
                                .class),
                        supplierName));
            }
            if (!StringUtils.isEmpty(orderNo)) {
                predicate = cb.and(predicate, cb.like(root.get("orderNo").as(String.class),
                        orderNo));
            }
            if (!StringUtils.isEmpty(touristName)) {
                predicate = cb.and(predicate, cb.like(root.get("touristGood").get("touristName").as(String.class),
                        touristName));
            }
            if (!StringUtils.isEmpty(buyerName)) {
                predicate = cb.and(predicate, cb.like(root.get("touristBuyer").get("buyerName").as(String.class),
                        buyerName));
            }
            if (!StringUtils.isEmpty(tel)) {
                predicate = cb.and(predicate, cb.like(root.get("touristBuyer").get("telPhone").as(String.class),
                        tel));
            }
            if (!StringUtils.isEmpty(payTypeEnum)) {
                predicate = cb.and(predicate, cb.equal(root.get("payType").as(PayTypeEnum.class),
                        payTypeEnum));
            }
            if (!StringUtils.isEmpty(orderStateEnum)) {
                predicate = cb.and(predicate, cb.equal(root.get("orderState").as(OrderStateEnum.class),
                        orderStateEnum));
            }
            if (orderDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createTime").as(LocalDateTime.class), orderDate));
            }

            if (endOrderDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createTime").as(LocalDateTime.class), endOrderDate));
            }

            if (payDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("payTime").as(LocalDateTime.class), payDate));
            }

            if (endPayDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("payTime").as(LocalDateTime.class), endPayDate));
            }
            if (touristDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.joinSet("travelers").get("route").get("fromDate")
                        .as(LocalDateTime.class), touristDate));
            }
            if(endTouristDate!=null){
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.joinSet("travelers").get("route").get("fromDate")
                        .as(LocalDateTime.class), endTouristDate));
            }
            return predicate;
        }, pageable);
    }

    @Override
    public BigDecimal countMoneyTotal(Long supplierId) throws IOException {
        if (supplierId == null) {
            throw new IOException();
        }
        return touristOrderRepository.sumMoneyTotal(supplierId);
    }

    @Override
    public BigDecimal countCommissionTotal(Long supplierId) throws IOException {
        if (supplierId == null) {
            throw new IOException();
        }
        return touristOrderRepository.sumCommissionTotal(supplierId);
    }

    @Override
    public BigDecimal countRefundTotal(Long supplierId) throws IOException {
        if (supplierId == null) {
            throw new IOException();
        }
        return touristOrderRepository.sumRefundTotal(supplierId, OrderStateEnum.RefundsFinish);
    }

    @Override
    public long countOrderTotal(Long supplierId) throws IOException {
        if (supplierId == null) {
            throw new IOException();
        }
        return touristOrderRepository.countByTouristGood_TouristSupplier_id(supplierId);
    }

    @Override
    public boolean checkOrderStatusCanBeModified(SystemUser user, OrderStateEnum formerStatus, OrderStateEnum laterStatus) {
        /**
         * 该订单是否可以被修改到相应订单状态
         */
        if(!Arrays.asList(OrderStateQuery.revisability[(int)formerStatus.getCode()]).contains(laterStatus)){
            return false;
        }
        /**
         * 该角色是否拥有修改到相应订单状态的权利
         */
        if(!Arrays.asList(OrderStateQuery.getAuthOrderStates(user)).contains(laterStatus)){
            return false;
        }
        return true;
    }

    @Override
    public List<OrderStateEnum> getModifyState(SystemUser user, TouristOrder touristOrder) {
        //获取该权限可以修改的订单状态
        OrderStateEnum[] orderStates= OrderStateQuery.getAuthOrderStates(user);
        OrderStateEnum orderformerState=touristOrder.getOrderState();
        List<OrderStateEnum> orderlaterStates=new ArrayList<>();
        //筛选可以被修改到之后的订单状态
        for(OrderStateEnum orderlater:orderStates){
            if(Arrays.asList(OrderStateQuery.revisability[(int)orderformerState.getCode()]).contains(orderlater)){
                orderlaterStates.add(orderlater);
            }
        }
        return orderlaterStates;
    }

    @Override
    public TouristOrder addOrderInfo(TouristBuyer user, List<Traveler> travelers, Long goodId, Long routeId
            , Float mallIntegral, Float mallBalance, Float mallCoffers, String remark) throws IOException, IllegalStateException {
        if (travelers != null && travelers.size() > 0) {
            TouristGood good = touristGoodRepository.getOne(goodId);
            TouristRoute route = touristRouteRepository.getOne(routeId);
            int num = travelerRepository.countByRoute(route);
            if (good.getMaxPeople() - num >= travelers.size()) {
                //todo 锁定线路不允许被其他游客同时添加
                Random random = new Random(10000);
                TouristOrder order = new TouristOrder();
                order.setTouristGood(good);
                order.setOrderState(OrderStateEnum.NotPay);
                order.setSettlement(false);
                order.setOrderNo(random.nextInt() + LocalDateTimeFormatter.toStr(LocalDateTime.now()) + random.nextInt());
                order.setCreateTime(LocalDateTime.now());
                order.setRemarks(remark);
                BigDecimal adult = good.getPrice().multiply(new BigDecimal(travelers.stream().filter(traveler ->
                        traveler
                                .getTravelerType().equals
                                (TravelerTypeEnum.adult)).count()));
                BigDecimal child = good.getPrice()
                        .multiply(good.getChildrenDiscount().divide(new BigDecimal(10)))
                        .multiply(new BigDecimal(travelers.stream()
                                .filter(traveler -> traveler.getTravelerType().equals(TravelerTypeEnum.children))
                                .count()
                        ));
                BigDecimal sumNum = adult.add(child);
                order.setMallIntegral(new BigDecimal(mallIntegral));
                order.setMallBalance(new BigDecimal(mallBalance));
                order.setMallCoffers(new BigDecimal(mallCoffers));
                order.setOrderMoney(sumNum);
                order.setTouristBuyer(user);
                for (Traveler traveler : travelers) {
                    traveler.setCreateTime(LocalDateTime.now());
                    traveler.setRoute(route);
                    traveler.setOrder(order);
                }
                order.setTravelers(travelers);
                order = touristOrderRepository.saveAndFlush(order);
                travelerRepository.save(travelers);
                return order;
            }
            return null;
        } else {
            throw new IOException("游客不能为空");
        }
    }

    @Override
    public void addOrderInfo(TouristBuyer user, List<Traveler> travelers, Long goodId, Long routeId, Float mallIntegral, Float mallBalance, Float mallCoffers) {

    }

    @Override
    public List<TouristOrder> getBuyerOrders(Long buyerId, Long lastId, String states) throws IOException {

        return touristOrderRepository.findAll((root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("touristBuyer").get("id").as(Long.class), buyerId);
            if(lastId!=null){
                predicate=cb.and(predicate,cb.lessThan(root.get("id").as(Long.class),lastId));
            }
            if("going".equals(states)){
                predicate=cb.and(predicate,root.get("orderState").as(OrderStateEnum.class)
                        .in(Arrays.asList(OrderStateEnum.NotPay
                        , OrderStateEnum.PayFinish,OrderStateEnum.NotFinish,OrderStateEnum.Refunds)));
            }else if("finish".equals(states)){
                predicate=cb.and(predicate,root.get("orderState").as(OrderStateEnum.class)
                        .in(Arrays.asList(OrderStateEnum.Finish,OrderStateEnum.RefundsFinish)));
            }else if ("cancel".equals(states)){
                predicate=cb.and(predicate,cb.equal(
                        root.get("orderState").as(OrderStateEnum.class),OrderStateEnum.Invalid));
            }
            return predicate;
        }, new PageRequest(0,20,new Sort(Sort.Direction.DESC,"id"))).getContent();
    }
}
