/*
 * 版权所有:杭州火图科技有限公司
 * 地址:浙江省杭州市滨江区西兴街道阡陌路智慧E谷B幢4楼
 *
 * (c) Copyright Hangzhou Hot Technology Co., Ltd.
 * Floor 4,Block B,Wisdom E Valley,Qianmo Road,Binjiang District
 * 2013-2016. All rights reserved.
 */

package com.huotu.tourist.controller;

import com.huotu.tourist.common.OrderStateEnum;
import com.huotu.tourist.common.PayTypeEnum;
import com.huotu.tourist.common.SexEnum;
import com.huotu.tourist.common.TouristCheckStateEnum;
import com.huotu.tourist.converter.LocalDateTimeFormatter;
import com.huotu.tourist.entity.ActivityType;
import com.huotu.tourist.entity.TouristGood;
import com.huotu.tourist.entity.TouristOrder;
import com.huotu.tourist.entity.TouristRoute;
import com.huotu.tourist.entity.TouristSupplier;
import com.huotu.tourist.entity.TouristType;
import com.huotu.tourist.entity.Traveler;
import com.huotu.tourist.login.SystemUser;
import com.huotu.tourist.model.PageAndSelection;
import com.huotu.tourist.model.Selection;
import com.huotu.tourist.repository.ActivityTypeRepository;
import com.huotu.tourist.repository.TouristGoodRepository;
import com.huotu.tourist.repository.TouristOrderRepository;
import com.huotu.tourist.repository.TouristRouteRepository;
import com.huotu.tourist.repository.TouristSupplierRepository;
import com.huotu.tourist.repository.TouristTypeRepository;
import com.huotu.tourist.repository.TravelerRepository;
import com.huotu.tourist.service.ActivityTypeService;
import com.huotu.tourist.service.PurchaserPaymentRecordService;
import com.huotu.tourist.service.TouristGoodService;
import com.huotu.tourist.service.TouristOrderService;
import com.huotu.tourist.service.TouristRouteService;
import com.huotu.tourist.service.TouristTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 提供公共的controller 方法
 * Created by lhx on 2016/12/21.
 */
@RequestMapping(value = "/base/")
public class BaseController {

    @Autowired
    public TravelerRepository travelerRepository;
    @Autowired
    public TouristSupplierRepository touristSupplierRepository;
    @Autowired
    public TouristOrderService touristOrderService;
    @Autowired
    public TouristOrderRepository touristOrderRepository;
    @Autowired
    public TouristRouteRepository touristRouteRepository;
    @Autowired
    public TouristRouteService touristRouteService;
    @Autowired
    public TouristGoodService touristGoodService;

    @Autowired
    public TouristGoodRepository touristGoodRepository;

    @Autowired
    public PurchaserPaymentRecordService purchaserPaymentRecordService;

    @Autowired
    public ActivityTypeService activityTypeService;

    @Autowired
    public TouristTypeService touristTypeService;

    @Autowired
    private TouristTypeRepository touristTypeRepository;

    @Autowired
    private ActivityTypeRepository activityTypeRepository;


    /**
     * 打开线路商品页面
     * @param model
     * @return
     */
    @RequestMapping("/showGoodsList")
    public String showGoodsList(Model model){

        List<TouristType> touristTypes=touristTypeRepository.findAll();

        List<ActivityType> activityTypes=activityTypeRepository.findAll();

        TouristCheckStateEnum[] checkStates=TouristCheckStateEnum.values();

        model.addAttribute("touristTypes",touristTypes);
        model.addAttribute("activityTypes",activityTypes);
        model.addAttribute("checkStates",checkStates);

        return "/view/supplier/goodsList";
    };



    /**
     * 订单列表
     * // TODO: 2016/12/22 加入用户体系，接口最大化
     *  @param user
     * @param orderNo      订单号
     * @param supplierName
     * @param touristName  线路名称
     * @param buyerName    采购商名称
     * @param tel          采购商电话
     * @param payType      支付类型
     * @param orderDate    开始订单创建时间
     * @param endOrderDate 结束订单创建时间
     * @param payDate      开始支付时间
     * @param endPayDate   结束支付时间
     * @param touristDate  线路开始时间
     * @param orderState   订单状态
     * @param pageSize     每页显示条数
     * @param pageNo       页码
     * @param request
     * @param model        @return
     */
    @RequestMapping(value = "touristOrders", method = RequestMethod.GET)
    public PageAndSelection touristOrders(@AuthenticationPrincipal SystemUser user, String orderNo,
                                          String supplierName, String touristName
            , String buyerName, String tel, PayTypeEnum payType, LocalDate orderDate, LocalDate endOrderDate
            , LocalDate payDate, LocalDate endPayDate, LocalDate touristDate, OrderStateEnum orderState
            , int pageSize, int pageNo, HttpServletRequest request, Model model) throws IOException {
        TouristSupplier supplier = null;
        if (user.isSupplier()) {
            supplier = (TouristSupplier) user;
        }
        Page<TouristOrder> page = touristOrderService.touristOrders(supplier, supplierName, orderNo, touristName, buyerName, tel,
                payType, orderDate, endOrderDate, payDate, endPayDate, touristDate, orderState,
                new PageRequest(pageNo, pageSize));
        List<Selection<TouristOrder, ?>> selections = new ArrayList<>();

        //出行时间特殊处理
        Selection<TouristOrder, LocalDateTime> touristDateSelection = new Selection<TouristOrder, LocalDateTime>() {
            @Override
            public String getName() {
                return "touristDate";
            }

            @Override
            public LocalDateTime apply(TouristOrder order) {
                Traveler traveler = travelerRepository.findByOrder_Id(order.getId()).get(0);
                return traveler.getRoute().getFromDate();
            }
        };

        //人数处理
        Selection<TouristOrder, Long> peopleNumberSelection = new Selection<TouristOrder, Long>() {
            @Override
            public String getName() {
                return "peopleNumber";
            }

            @Override
            public Long apply(TouristOrder order) {
                return travelerRepository.countByOrder_Id(order.getId());
            }
        };

        //行程ID
        Selection<TouristOrder,Long> touristRouteIdSelection=new Selection<TouristOrder, Long>() {
            @Override
            public String getName() {
                return "touristRouteId";
            }

            @Override
            public Long apply(TouristOrder order) {
                return travelerRepository.findByOrder_Id(order.getId()).get(0).getId();
            }
        };
        selections.add(touristDateSelection);
        selections.add(peopleNumberSelection);
        selections.add(touristRouteIdSelection);
        selections.addAll(TouristOrder.htmlSelections);


        return new PageAndSelection<>(page,selections);
    }

    /**
     * 导出订单列表
     *  @param user
     * @param supplierName
     * @param orderNo      订单号
     * @param touristName  线路名称
     * @param buyerName    采购商名称
     * @param tel          采购商电话
     * @param payType      支付类型
     * @param orderDate    开始订单创建时间
     * @param endOrderDate 结束订单创建时间
     * @param payDate      开始支付时间
     * @param endPayDate   结束支付时间
     * @param touristDate  线路开始时间
     * @param orderState   订单状态
     * @param pageSize     每页显示条数
     * @param pageNo       页码
     * @param request
     * @param model        @return
     */
    @RequestMapping(value = "exportTouristOrdersOrders", method = RequestMethod.GET)
    public ResponseEntity exportTouristOrdersOrders(@AuthenticationPrincipal SystemUser user
            , String supplierName, String orderNo, String touristName, String buyerName, String tel, PayTypeEnum payType, LocalDate orderDate
            , LocalDate endOrderDate, LocalDate payDate, LocalDate endPayDate, LocalDate touristDate
            , OrderStateEnum orderState, int pageSize, int pageNo, HttpServletRequest request, Model model) throws IOException {
        TouristSupplier supplier = null;
        if (user.isSupplier()) {
            supplier = (TouristSupplier) user;
        }
        Page<TouristOrder> page = touristOrderService.touristOrders(supplier, supplierName, orderNo, touristName, buyerName, tel,
                payType, orderDate, endOrderDate, payDate, endPayDate, touristDate, orderState,
                new PageRequest(pageNo, pageSize));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "订单记录.csv");
        StringBuffer sb = new StringBuffer();
        sb.append("订单号,").append("下单时间,").append("支付时间,").append("支付方式,").append("供应商,").append("线路名称,")
                .append("金额,").append("购买人,").append("支付状态,").append("出行时间,").append("购买数量,").append("备注/n");
        for (TouristOrder touristOrder : page.getContent()) {
            List<Traveler> travelers = travelerRepository.findByOrder_Id(touristOrder.getId());
            sb
                    .append(touristOrder.getOrderNo()).append(",")
                    .append(LocalDateTimeFormatter.toStr(touristOrder.getCreateTime())).append(",")
                    .append(LocalDateTimeFormatter.toStr(touristOrder.getPayTime())).append(",")
                    .append(touristOrder.getPayType().getValue()).append(",")
                    .append(touristOrder.getTouristGood().getTouristSupplier().getSupplierName()).append(",")
                    .append(touristOrder.getTouristGood().getTouristName()).append(",")
                    .append(touristOrder.getOrderMoney()).append(",").append(touristOrder.getTouristBuyer().getBuyerName())
                    .append("/r").append(touristOrder.getTouristBuyer().getTelPhone()).append(",")
                    .append(touristOrder.getOrderState()).append(",")
                    .append(LocalDateTimeFormatter.toStr(travelers.get(0).getRoute().getFromDate())).append(",")
                    .append(travelers.size()).append(",")
                    .append(touristOrder.getRemarks());
        }
        return new ResponseEntity<>(sb.toString().getBytes("utf-8"), headers, HttpStatus.CREATED);
    }

    /**
     * 线路列表/推荐线路列表通过是否推荐属性进行判断是否推荐
     * // TODO: 2016/12/22 加入用户体系
     *
     * @param user
     * @param touristName       线路名称
     * @param supplierName      供应商名称
     * @param touristTypeId     线路类型ID
     * @param activityTypeId    活动ID
     * @param touristCheckState 线路审核状态
     * @param recommend         是否推荐 null全部，true推荐列表
     * @param pageSize          每页显示条数
     * @param pageNo            页码
     * @param request
     */
    @RequestMapping(value = "touristGoodList", method = RequestMethod.GET)
    public PageAndSelection<TouristGood> touristGoodList(@AuthenticationPrincipal SystemUser user
            , String touristName, String supplierName, Long touristTypeId, Long activityTypeId
            , TouristCheckStateEnum touristCheckState, Boolean recommend, int pageSize, int pageNo
            , HttpServletRequest request) {
        TouristSupplier supplier = null;
        if (user.isSupplier()) {
            supplier = (TouristSupplier) user;
        }
        ActivityType activityType = null;
        TouristType touristType = null;
        if (touristTypeId != null) {
            touristType = touristTypeService.getOne(touristTypeId);
        }
        if (activityTypeId != null) {
            activityType = activityTypeService.getOne(touristTypeId);
        }
        Page<TouristGood> page;
        if (recommend) {
            page = touristGoodService.recommendTouristGoodList(touristName, supplierName, touristType, activityType
                    , touristCheckState, true, new PageRequest(pageNo, pageSize));
        } else {
            page = touristGoodService.touristGoodList(supplier, touristName, supplierName, touristType,
                    activityType, touristCheckState, new PageRequest(pageNo, pageSize));
        }
        List<Selection<TouristGood, ?>> selects = new ArrayList<>();
        selects.addAll(TouristGood.selections);
        selects.add(TouristGood.select);
        return new PageAndSelection<>(page, selects);
    }

    /**
     * 修改订单备注
     *
     * @param id     线路订单号
     * @param remark 新的备注
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/modifyOrderRemarks", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public void modifyOrderRemarks(@RequestParam Long id, @RequestParam String remark) throws IOException {
        TouristOrder touristOrder = touristOrderRepository.getOne(id);
        touristOrder.setRemarks(remark);
    }


    /**
     * 修改订单状态
     *
     * @param id         订单ID
     * @param user       当前的用户
     * @param orderState 新的订单状态
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/modifyOrderState", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public void modifyOrderState(@AuthenticationPrincipal SystemUser user,@RequestParam Long id, @RequestParam
    OrderStateEnum orderState) throws IOException {
        TouristOrder order = touristOrderRepository.getOne(id);
        if(touristOrderService.checkOrderStatusCanBeModified(user,order.getOrderState(),orderState)){
            order.setOrderState(orderState);
        }
    }

    /**
     * 修改游客的个人信息
     *
     * @param id   游客ID(必须)
     * @param name 游客姓名
     * @param sex  性别
     * @param age  年龄
     * @param tel  电话
     * @param IDNo 身份证号
     * @throws IOException
     */
    @RequestMapping(value = "/modifyTravelerBaseInfo", method = RequestMethod.POST)
    @Transactional
    public void modifyTravelerBaseInfo(@RequestParam Long id, @RequestParam String name, SexEnum sex, Integer age, @RequestParam
            String tel, @RequestParam String IDNo) throws IOException {
        Traveler traveler = travelerRepository.getOne(id);
        traveler.setName(name);
        traveler.setSex(sex);
        traveler.setAge(age);
        traveler.setTelPhone(tel);
        traveler.setIDNo(IDNo);
    }

    /**
     * 修改游客的线路行程
     *
     * @param formerId 原先的线路行程ID
     * @param laterId  之后的线路行程ID
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/modifyOrderTouristDate", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public void modifyOrderTouristDate(@RequestParam Long formerId, @RequestParam Long laterId) throws IOException {
        travelerRepository.modifyRouteIdByRouteId(laterId, formerId);
    }

    /**
     * 显示线路商品
     *
     * @param id    商品ID
     * @param model 返回的model
     * @return
     * @throws IOException
     */
    @RequestMapping("/showTouristGood")
    public String showTouristGood(@RequestParam Long id, Model model) throws IOException {
        TouristGood touristGood = touristGoodRepository.findOne(id);
        List<TouristRoute> routes = touristRouteRepository.findByGood(touristGood);
        model.addAttribute("routes", routes);
        model.addAttribute("good", touristGood);
        return null;
    }

    /**
     * 修改线路商品的状态
     *
     * @param user
     * @param id         线路商品ID
     * @param checkState 状态
     * @throws IOException
     */
    @RequestMapping("/modifyTouristGoodState")
    @ResponseBody
    @Transactional
    public ResponseEntity modifyTouristGoodState(@AuthenticationPrincipal SystemUser user, @RequestParam Long id,
                                                 @RequestParam TouristCheckStateEnum checkState) throws IOException {
        TouristGood touristGood = touristGoodRepository.getOne(id);
        if (user.isPlatformUser()) {
            if (touristGood.getTouristCheckState().equals(TouristCheckStateEnum.NotChecking) &&
                    checkState.equals(TouristCheckStateEnum.CheckFinish)) {
                touristGood.setTouristCheckState(checkState);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body("{msg:'当前状态不能进行审核通过'}");
            }
            return ResponseEntity.ok().build();
        }

        if (user.isSupplier() && !checkState.equals(TouristCheckStateEnum.CheckFinish)) {
            touristGood.setTouristCheckState(checkState);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body("{msg:'当前状态不能进行审核通过'}");
        }
    }


}
