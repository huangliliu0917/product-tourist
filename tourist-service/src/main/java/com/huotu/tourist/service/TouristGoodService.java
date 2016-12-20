package com.huotu.tourist.service;

import com.huotu.tourist.common.TouristCheckStateEnum;
import com.huotu.tourist.entity.ActivityType;
import com.huotu.tourist.entity.TouristGood;
import com.huotu.tourist.entity.TouristType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 线路相关服务
 * Created by lhx on 2016/12/17.
 */

public interface TouristGoodService extends BaseService<TouristGood, Long> {

    /**
     * 线路列表
     *
     * @param touristName       线路名称 可以为null
     * @param supplierId        供应商ID
     * @param supplierName      供应商名称
     * @param touristType       路线类型
     * @param activityType      活动类型
     * @param touristCheckState 线路审核状态
     * @param pageable
     * @return
     */
    Page<TouristGood> touristGoodList(Long supplierId,String touristName, String supplierName, TouristType touristType
            , ActivityType activityType, TouristCheckStateEnum touristCheckState, Pageable pageable);

    /**
     * 获取推荐商品列表
     *
     * @param pageable
     * @return
     */
    Page<TouristGood> recommendTouristGoodList(String touristName, String supplierName, TouristType touristType
            , ActivityType activityType, TouristCheckStateEnum touristCheckState, Boolean recommend, Pageable pageable);


}
