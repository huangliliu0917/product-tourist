/*
 * 版权所有:杭州火图科技有限公司
 * 地址:浙江省杭州市滨江区西兴街道阡陌路智慧E谷B幢4楼
 *
 * (c) Copyright Hangzhou Hot Technology Co., Ltd.
 * Floor 4,Block B,Wisdom E Valley,Qianmo Road,Binjiang District
 * 2013-2017. All rights reserved.
 */

package com.huotu.tourist.controller.wap;

import com.huotu.tourist.common.BuyerCheckStateEnum;
import com.huotu.tourist.common.BuyerPayStateEnum;
import com.huotu.tourist.common.TouristCheckStateEnum;
import com.huotu.tourist.entity.ActivityType;
import com.huotu.tourist.entity.Address;
import com.huotu.tourist.entity.Banner;
import com.huotu.tourist.entity.TouristBuyer;
import com.huotu.tourist.entity.TouristGood;
import com.huotu.tourist.entity.TouristRoute;
import com.huotu.tourist.entity.TouristType;
import com.huotu.tourist.model.VerificationType;
import com.huotu.tourist.repository.ActivityTypeRepository;
import com.huotu.tourist.repository.BannerRepository;
import com.huotu.tourist.repository.PurchaserProductSettingRepository;
import com.huotu.tourist.repository.TouristBuyerRepository;
import com.huotu.tourist.repository.TouristGoodRepository;
import com.huotu.tourist.repository.TouristOrderRepository;
import com.huotu.tourist.repository.TouristRouteRepository;
import com.huotu.tourist.repository.TouristSupplierRepository;
import com.huotu.tourist.repository.TouristTypeRepository;
import com.huotu.tourist.repository.TravelerRepository;
import com.huotu.tourist.service.ActivityTypeService;
import com.huotu.tourist.service.ConnectMallService;
import com.huotu.tourist.service.PurchaserPaymentRecordService;
import com.huotu.tourist.service.TouristGoodService;
import com.huotu.tourist.service.TouristOrderService;
import com.huotu.tourist.service.TouristRouteService;
import com.huotu.tourist.service.TouristTypeService;
import com.huotu.tourist.service.VerificationCodeService;
import me.jiangcai.lib.resource.service.ResourceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * index controller 方法
 * Created by lhx on 2016/12/21.
 */
@Controller
@RequestMapping(value = "/wap/")
public class IndexController {
    private static final Log log = LogFactory.getLog(IndexController.class);
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
    ResourceService resourceService;
    @Autowired
    TouristBuyerRepository touristBuyerRepository;
    @Autowired
    PurchaserProductSettingRepository purchaserProductSettingRepository;
    @Autowired
    VerificationCodeService verificationCodeService;
    @Autowired
    TouristTypeRepository touristTypeRepository;
    @Autowired
    private ActivityTypeRepository activityTypeRepository;
    @Autowired
    private BannerRepository bannerRepository;
    @Autowired
    private ConnectMallService connectMallService;

    /**
     * 打开index
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/", "index/"})
    public String index(Model model) {
        List<Banner> banners = bannerRepository.findByDeletedFalse(new PageRequest(0, 5)).getContent();
        model.addAttribute("banners", banners);
        List<TouristGood> recommendGoods = touristGoodRepository
                .findByRecommendTrueAndDeletedFalseAndTouristCheckState(new PageRequest(0, 3), TouristCheckStateEnum
                        .CheckFinish).getContent();
        model.addAttribute("recommendGoods", recommendGoods);
        List<ActivityType> activityTypes = activityTypeRepository.findByDeletedFalse(new PageRequest(0, 9)).getContent();
        model.addAttribute("activityTypes", activityTypes);
        List<TouristType> cty = touristTypeRepository.findByTypeName("长途游");
        List<TouristType> dty = touristTypeRepository.findByTypeName("短途游");
        model.addAttribute("cty", cty.get(0).getId());
        model.addAttribute("dty", dty.get(0).getId());
        return "view/wap/index.html";
    }

    /**
     * 打开商品详细信息
     * @param model
     * @return
     */
    @RequestMapping(value = {"/goodInfo"})
    public String goodInfo(@AuthenticationPrincipal TouristBuyer user, @RequestParam Long id, Model model) {
        TouristGood good = touristGoodRepository.getOne(id);
        //过滤出有效的行程
        good.setTouristRoutes(good.getTouristRoutes().stream().filter(route ->
                route.getFromDate().isAfter(LocalDateTime.now())).collect(Collectors.toList()));

        Map<String, Integer> routeCount = new HashMap<>();
        for (TouristRoute touristRoute : good.getTouristRoutes()) {
            int num = travelerRepository.countByRoute(touristRoute);
            routeCount.put(touristRoute.getId().toString(), touristRoute.getMaxPeople() - num);
        }
        model.addAttribute("good", good);
        model.addAttribute("routeCount", routeCount);
        int count = touristGoodRepository.countByTouristSupplier_IdAndDeletedFalseAndTouristCheckState(
                good.getTouristSupplier().getId(), TouristCheckStateEnum.CheckFinish);
        user = touristBuyerRepository.findOne(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("count", count);
        return "/view/wap/touristGoodInfo.html";
    }

    /**
     * 打开购买商品填写订单信息页面
     *
     * @param goodId
     * @param routeId
     * @param model
     * @return
     */
    @RequestMapping(value = {"/procurementGood"})
    public String procurementGood(@AuthenticationPrincipal TouristBuyer user, @RequestParam Long goodId, Long routeId
            , Model model) {
        TouristGood good = touristGoodRepository.getOne(goodId);
        int count = travelerRepository.countByRoute(touristRouteRepository.getOne(routeId));
        model.addAttribute("amount", good.getMaxPeople() - count);
        model.addAttribute("good", good);
        model.addAttribute("routeId", routeId);
        Map userInfo;
        try {
            userInfo = connectMallService.getUserDetailByUserId(user.getId());
            model.addAttribute("mallIntegral", userInfo.get("score"));
            model.addAttribute("mallBalance", userInfo.get("money"));
            model.addAttribute("mallCoffers", userInfo.get("gold"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "view/wap/procurement.html";
    }

    /**
     * 最新线路列表
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/newTouristList"})
    public String newTouristList(int offset, Model model) {
        List<TouristGood> list = touristGoodService.findNewTourists(offset);
        model.addAttribute("list", list);
        model.addAttribute("offset", offset);
        return "view/wap/newTourist.html";
    }

    /**
     * 打开活动类型的列表界面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/activityTypeList"})
    public String activityTypeList(Model model) {
        model.addAttribute("activityTypes", activityTypeRepository.findAll());
        return "view/wap/activityTypeList.html";
    }

    /**
     * 打开指定活动类型的商品列表界面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/activityTypeGoods"})
    public String activityTypeGoods(@RequestParam Long activityTypeId, Model model) {
        model.addAttribute("activityTypeId", activityTypeId);
        return "view/wap/activityTypeGoods.html";
    }

    /**
     * 指定活动类型的商品列表界面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/activityTourist"})
    public String activityTourist(@RequestParam Long activityTypeId, int offset, Model model) {
        int page = (int) Math.ceil(offset / 10F);
        model.addAttribute("activityTypeGoods", touristGoodRepository
                .findByActivityType_IdAndDeletedFalseAndTouristCheckState(activityTypeId, new
                        PageRequest(page, 10), TouristCheckStateEnum.CheckFinish).getContent());
        model.addAttribute("activityTypeId", activityTypeId);
        model.addAttribute("offset", offset);
        return "view/wap/activityTourist.html";
    }

    /**
     * 打开指定供应商的商品列表界面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/supplierGoods"})
    public String supplierGoods(@RequestParam Long supplierId, Model model) {
        int count = touristGoodRepository.countByTouristSupplier_IdAndDeletedFalseAndTouristCheckState(supplierId
                , TouristCheckStateEnum.CheckFinish);
        model.addAttribute("count", count);
        model.addAttribute("supplier", touristSupplierRepository.getOne(supplierId));
        return "view/wap/touristSupplier.html";
    }

    /**
     * 指定供应商的商品列表界面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/supplierTourist"})
    public String supplierTourist(@RequestParam Long supplierId, int offset, Model model) {
        int page = (int) Math.ceil(offset / 10F);
        model.addAttribute("supplierGoods", touristGoodRepository
                .findByTouristSupplier_IdAndDeletedFalseAndTouristCheckState(supplierId, new
                        PageRequest(page, 10), TouristCheckStateEnum.CheckFinish).getContent());
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("offset", offset);
        return "view/wap/supplierTourist.html";
    }

    /**
     * 指定目的地列表界面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/destinationList"})
    public String destinationList(Model model) {
        List<Address> towns = touristGoodService.findByDestinationTown();
        Map<String, List<Address>> maps = towns.stream().collect(Collectors.groupingBy(address ->
                address.getProvince()));
        model.addAttribute("destinationMaps", maps);
        return "view/wap/destination.html";
    }

    /**
     * 打开指定目的地商品列表界面
     *
     * @param type  = 0 , 1  省份，市区
     * @param model
     * @return
     */
    @RequestMapping(value = {"/destinationGoods"})
    public String destinationGoods(@RequestParam Long type, @RequestParam String value, Model model) {
        model.addAttribute("type", type);
        model.addAttribute("value", value);
        return "view/wap/destinationGoods.html";
    }

    /**
     * 指定目的地商品列表
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/destinationTourist"})
    public String destinationTourist(@RequestParam Long type, @RequestParam String value, int offset, Model model) {
        int page = (int) Math.ceil(offset / 10F);
        if (type == 0) {
            model.addAttribute("destinationGoods", touristGoodRepository
                    .findByDestination_ProvinceAndDeletedFalseAndTouristCheckState(value, new
                            PageRequest(page, 10), TouristCheckStateEnum.CheckFinish).getContent());
        } else {
            model.addAttribute("destinationGoods", touristGoodRepository
                    .findByDestination_TownAndDeletedFalseAndTouristCheckState(value, new
                            PageRequest(page, 10), TouristCheckStateEnum.CheckFinish).getContent());
        }
        model.addAttribute("type", type);
        model.addAttribute("value", value);
        model.addAttribute("offset", offset);
        return "view/wap/destinationTourist.html";
    }

    /**
     * 打开热点推荐商品列表界面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/recommendGoods"})
    public String recommendGoods(Model model) {
        return "view/wap/recommendGoods.html";
    }

    /**
     * 打开热点推荐商品列表界面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/recommendTourist"})
    public String recommendTourist(@RequestParam int offset, Model model) {
        int page = (int) Math.ceil(offset / 10F);
        model.addAttribute("recommendGoods", touristGoodRepository
                .findByRecommendTrueAndDeletedFalseAndTouristCheckState(new
                        PageRequest(page, 10), TouristCheckStateEnum.CheckFinish).getContent());
        model.addAttribute("offset", offset);
        return "view/wap/recommendTourist.html";
    }


    /**
     * 打开申请成为采购商界面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/buyerApply"})
    public String buyerApply(Model model) {
        return "view/wap/buyerApply.html";
    }


    /**
     * 提交采购商申请
     *
     * @param buyerName           采购商姓名
     * @param buyerDirector       负责人姓名
     * @param telPhone            电话
     * @param IDNo                身份证
     * @param businessLicencesUri 营业执照
     * @param IDElevationsUri     身份证正面
     * @param IDInverseUri        身份证背面
     * @param model
     * @return
     * @throws IOException 上传图片异常
     */
    @RequestMapping(value = {"/addTouristBuyer"}, method = RequestMethod.POST)
    public String addTouristBuyer(@AuthenticationPrincipal TouristBuyer buyer, @RequestParam
            String buyerName, @RequestParam String buyerDirector
            , @RequestParam String telPhone, @RequestParam String IDNo, @RequestParam MultipartFile businessLicencesUri
            , @RequestParam MultipartFile IDElevationsUri, @RequestParam MultipartFile IDInverseUri, Model model) throws IOException {
        TouristBuyer touristBuyer = touristBuyerRepository.getOne(buyer.getId());
        touristBuyer.setCheckState(BuyerCheckStateEnum.Checking);
        touristBuyer.setTelPhone(telPhone);
        touristBuyer.setBuyerDirector(buyerDirector);
        touristBuyer.setBuyerName(buyerName);
        String businessLicencesUriFilename = "buyer/" + UUID.randomUUID().toString() + businessLicencesUri.getOriginalFilename();
        resourceService.uploadResource(businessLicencesUriFilename, businessLicencesUri.getInputStream());
        touristBuyer.setBusinessLicencesUri(businessLicencesUriFilename);
        String IDElevationsUriFilename = "buyer/" + UUID.randomUUID().toString() + IDElevationsUri.getOriginalFilename();
        resourceService.uploadResource(IDElevationsUriFilename, IDElevationsUri.getInputStream());
        touristBuyer.setIDElevationsUri(IDElevationsUriFilename);
        String idInverseUri = "buyer/" + UUID.randomUUID().toString() + IDInverseUri.getOriginalFilename();
        resourceService.uploadResource(idInverseUri, IDInverseUri.getInputStream());
        touristBuyer.setIDInverseUri(idInverseUri);
        touristBuyer.setIDNo(IDNo);
        touristBuyer.setPayState(BuyerPayStateEnum.NotPay);
        touristBuyer.setCreateTime(LocalDateTime.now());
        touristBuyerRepository.saveAndFlush(touristBuyer);
        return "view/wap/msg.html";
    }


    /**
     * 长途游短途游下拉列表界面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/mddxl"})
    public String mddxl(Integer offset, String[] cityNames, Integer[] sorts, Long[] activityIds
            , Long[] touristTypeIds, Model model) {
        List<Address> towns = touristGoodService.findByDestinationTown();
        Map<String, List<Address>> maps = towns.stream().collect(Collectors.groupingBy(address ->
                address.getProvince()));

        model.addAttribute("destinationMaps", maps);
        List<ActivityType> activityTypes = activityTypeRepository.findAll();
        model.addAttribute("activityTypes", activityTypes);
        List<TouristType> touristTypes = touristTypeRepository.findAll();
        model.addAttribute("touristTypes", touristTypes);

        model.addAttribute("offset", offset);
        List<String> cityNameList = new ArrayList<>();
        if (cityNames != null && cityNames.length > 0) {
            String cityNameStr = "";
            cityNameList = Arrays.asList(cityNames);
            for (String cityName : cityNames) {
                cityNameStr += cityName + ",";
            }
            model.addAttribute("cityNames", cityNameStr.substring(0, cityNameStr.length() - 1));
        }
        model.addAttribute("cityNameList", cityNameList);
        List<Integer> sortsList = new ArrayList<>();
        if (sorts != null && sorts.length > 0) {
            String sortStr = "";
            sortsList = Arrays.asList(sorts);
            for (Integer sort : sorts) {
                sortStr += sort + ",";
            }
            model.addAttribute("sorts", sortStr.substring(0, sortStr.length() - 1));
        }
        model.addAttribute("sortsList", sortsList);
        List<Long> activityIdList = new ArrayList<>();
        if (activityIds != null && activityIds.length > 0) {
            String activityIdStr = "";
            activityIdList = Arrays.asList(activityIds);
            for (Long id : activityIds) {
                activityIdStr += id + ",";
            }
            model.addAttribute("activityIds", activityIdStr.substring(0, activityIdStr.length() - 1));
        }
        model.addAttribute("activityIdList", activityIdList);
        List<Long> touristTypeIdList = new ArrayList<>();
        if (touristTypeIds != null && touristTypeIds.length > 0) {
            String touristTypeIdStr = "";
            touristTypeIdList = Arrays.asList(touristTypeIds);
            for (Long id : touristTypeIds) {
                touristTypeIdStr += id + ",";
            }
            model.addAttribute("touristTypeIds", touristTypeIdStr.substring(0, touristTypeIdStr.length() - 1));
        }
        model.addAttribute("touristTypeIdList", touristTypeIdList);
        return "view/wap/mddxl.html";
    }

    @RequestMapping(value = {"/mddxlTourist"})
    public String mddxlTourist(@RequestParam int offset, String[] cityNames, Integer[] sorts, Long[] activityIds
            , Long[] touristTypeIds, Model model) {
        List<TouristGood> mddxlGoods = touristGoodService.findByMddxlTourist(cityNames, sorts, activityIds,
                touristTypeIds, offset);
        model.addAttribute("mddxlGoods", mddxlGoods);
        model.addAttribute("offset", offset);
        if (cityNames != null && cityNames.length > 0) {
            String cityNameStr = "";
            for (String cityName : cityNames) {
                cityNameStr += cityName + ",";
            }
            model.addAttribute("cityNames", cityNameStr.substring(0, cityNameStr.length() - 1));
        }
        if (sorts != null && sorts.length > 0) {
            String sortStr = "";
            for (Integer sort : sorts) {
                sortStr += sort + ",";
            }
            model.addAttribute("sorts", sortStr.substring(0, sortStr.length() - 1));
        }
        if (activityIds != null && activityIds.length > 0) {
            String activityIdStr = "";
            for (Long id : activityIds) {
                activityIdStr += id + ",";
            }
            model.addAttribute("activityIds", activityIdStr.substring(0, activityIdStr.length() - 1));
        }
        if (touristTypeIds != null && touristTypeIds.length > 0) {
            String touristTypeIdStr = "";
            for (Long id : touristTypeIds) {
                touristTypeIdStr += id + ",";
            }
            model.addAttribute("touristTypeIds", touristTypeIdStr.substring(0, touristTypeIdStr.length() - 1));
        }
        return "view/wap/mddxlTourist.html";
    }


    /**
     * 发送短信验证码
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/sendCode"}, method = RequestMethod.POST)
    @ResponseBody
    public void sendCode(String phone, Model model) throws IOException {
        verificationCodeService.sendCode(phone, VerificationType.register);
    }

    /**
     * 验证短信验证码
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/verifyCode"}, method = RequestMethod.POST)
    @ResponseBody
    public void verifyCode(String phone, String code, Model model) throws IOException {
        verificationCodeService.verify(phone, code, VerificationType.register);
    }

}
