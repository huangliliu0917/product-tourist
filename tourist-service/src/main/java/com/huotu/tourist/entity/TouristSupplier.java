/*
 * 版权所有:杭州火图科技有限公司
 * 地址:浙江省杭州市滨江区西兴街道阡陌路智慧E谷B幢4楼
 *
 * (c) Copyright Hangzhou Hot Technology Co., Ltd.
 * Floor 4,Block B,Wisdom E Valley,Qianmo Road,Binjiang District
 * 2013-2017. All rights reserved.
 */

package com.huotu.tourist.entity;

import com.huotu.tourist.converter.LocalDateTimeFormatter;
import com.huotu.tourist.login.Login;
import com.huotu.tourist.login.SystemUser;
import com.huotu.tourist.model.Selection;
import com.huotu.tourist.model.SimpleSelection;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 线路供应商
 * Created by lhx on 2016/12/16.
 */
@Entity
@Table(name = "Tourist_Supplier")
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
public class TouristSupplier extends Login implements SystemUser {

    public static final List<Selection<TouristSupplier, ?>> selections = Arrays.asList(
            new SimpleSelection<TouristSupplier, String>("id", "id"),
            new SimpleSelection<TouristSupplier, String>("supplierName", "supplierName")
            , new SimpleSelection<TouristSupplier, String>("loginName", "loginName")
            , new Selection<TouristSupplier, String>() {
                @Override
                public String apply(TouristSupplier touristSupplier) {
                    if (touristSupplier.getAddress() == null) {
                        return "";
                    }
                    return touristSupplier.getAddress().getProvince() + touristSupplier.getAddress().getTown()
                            + touristSupplier.getAddress().getDistrict();
                }

                @Override
                public String getName() {
                    return "address";
                }
            }
            , new SimpleSelection<TouristSupplier, String>("contacts", "contacts")
            , new SimpleSelection<TouristSupplier, String>("contactNumber", "contactNumber")
            , new SimpleSelection<TouristSupplier, String>("frozen", "frozen")
            , new Selection<TouristSupplier, String>() {
                @Override
                public String apply(TouristSupplier touristSupplier) {
                    return LocalDateTimeFormatter.toStr(touristSupplier.getCreateTime());
                }

                @Override
                public String getName() {
                    return "createTime";
                }
            }
    );
    /**
     * 供应商名称
     */
    @Column(length = 50)
    private String supplierName;

    /**
     * 所在地址
     */
    @Embedded
    private Address address;

    /**
     * 详细地址
     */
    @Column(length = 100)
    private String detailedAddress;

    /**
     * 联系人
     */
    @Column(length = 20)
    private String contacts;

    /**
     * 联系电话
     */
    @Column(length = 15)
    private String contactNumber;

    /**
     * 营业执照uri
     */
    @Column(length = 200)
    private String businessLicenseUri;
    /**
     * 备注
     */
    @Lob
    @Column
    private String remarks;

    /**
     * 结算余额
     * 当前余额 = 结算余额+所有已确认结算单总额-所有未被拒绝的提现订单总额
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal settlementBalance = BigDecimal.ZERO;

    /**
     * 冻结
     */
    @Column(insertable = false)
    private boolean frozen;

    @Override
    public boolean isSupplier() {
        return true;
    }

    @Override
    public boolean isPlatformUser() {
        return false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_SUPPLIER"));
    }
}
