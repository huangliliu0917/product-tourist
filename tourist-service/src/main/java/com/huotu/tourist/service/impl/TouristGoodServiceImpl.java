package com.huotu.tourist.service.impl;

import com.huotu.tourist.common.TouristCheckStateEnum;
import com.huotu.tourist.entity.ActivityType;
import com.huotu.tourist.entity.Address;
import com.huotu.tourist.entity.TouristGood;
import com.huotu.tourist.entity.TouristSupplier;
import com.huotu.tourist.entity.TouristType;
import com.huotu.tourist.repository.TouristGoodRepository;
import com.huotu.tourist.repository.TouristOrderRepository;
import com.huotu.tourist.service.TouristGoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lhx on 2017/1/3.
 */
@Service
public class TouristGoodServiceImpl implements TouristGoodService {
    @Autowired
    TouristGoodRepository touristGoodRepository;

    @Autowired
    TouristOrderRepository touristOrderRepository;

    @Override
    public TouristGood save(TouristGood data) {
        return touristGoodRepository.saveAndFlush(data);
    }

    @Override
    public TouristGood getOne(Long aLong) {
        return touristGoodRepository.getOne(aLong);
    }

    @Override
    public void delete(Long aLong) {
        touristGoodRepository.delete(aLong);
    }

    @Override
    public Page<TouristGood> touristGoodList(TouristSupplier supplier, String touristName, String supplierName
            , TouristType touristType, ActivityType activityType, TouristCheckStateEnum touristCheckState
            , Boolean recommend, Pageable pageable, Long lastId) {
        return touristGoodRepository.findAll((root, query, cb) -> {
            Predicate predicate = cb.isTrue(cb.literal(true));
            if (recommend != null) {
                predicate = cb.and(predicate, cb.equal(root.get("recommend").as(Boolean.class),
                        recommend));
            }
            if (lastId != null) {
                predicate = cb.and(predicate, cb.lessThan(root.get("id").as(Long.class), lastId));
            }
            if (supplier != null) {
                predicate = cb.and(cb.equal(root.get("touristSupplier").as(TouristSupplier.class), supplier));
            }
            if (!StringUtils.isEmpty(supplierName)) {
                predicate = cb.and(predicate, cb.like(root.get("touristSupplier").get("supplierName").as(String.class),
                        "%" + supplierName + "%"));
            }
            if (!StringUtils.isEmpty(touristName)) {
                predicate = cb.and(predicate, cb.like(root.get("touristName").as(String.class),
                        "%" + touristName + "%"));
            }
            if (touristType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("touristType").as(TouristType.class),
                        touristType));
            }
            if (activityType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("activityType").as(ActivityType.class),
                        activityType));
            }
            if (touristCheckState != null) {
                predicate = cb.and(predicate, cb.equal(root.get("touristCheckState").as(TouristCheckStateEnum.class),
                        touristCheckState));
            }
            return predicate;
        }, pageable);
    }

    @Override
    public Page<TouristGood> recommendTouristGoodList(String touristName, String supplierName, TouristType touristType
            , ActivityType activityType, TouristCheckStateEnum touristCheckState, Boolean recommend, Pageable pageable) {
        return touristGoodRepository.findAll((root, query, cb) -> {
            Predicate predicate = cb.isTrue(cb.literal(true));
            if (!StringUtils.isEmpty(supplierName)) {
                predicate = cb.and(predicate, cb.like(root.get("touristSupplier").get("supplierName").as(String.class),
                        supplierName));
            }
            if (!StringUtils.isEmpty(touristName)) {
                predicate = cb.and(predicate, cb.like(root.get("touristName").as(String.class),
                        touristName));
            }
            if (touristType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("touristType").as(TouristType.class),
                        touristType));
            }
            if (activityType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("activityType").as(ActivityType.class),
                        activityType));
            }
            if (touristCheckState != null) {
                predicate = cb.and(predicate, cb.equal(root.get("touristCheckState").as(TouristCheckStateEnum.class),
                        touristCheckState));
            }
            if (recommend != null) {
                predicate = cb.and(predicate, cb.equal(root.get("recommend").as(Boolean.class),
                        recommend));
            }
            return predicate;
        }, pageable);
    }

    @Override
    public Page<TouristGood> salesRanking(Long supplierId, Pageable pageable, LocalDateTime orderDate, LocalDateTime endOrderDate) {
        Page<Object> page = null;
        if (orderDate != null && endOrderDate != null) {
            touristOrderRepository.goodsSalesRankingByDate(supplierId, orderDate, endOrderDate, pageable);
        } else {
            touristOrderRepository.goodsSalesRanking(supplierId, pageable);
        }
        List<TouristGood> touristGoods = new ArrayList<>();
        for (Object o : page) {
            if (o == null) {
                continue;
            }
            Object[] objects = (Object[]) o;
            if (objects[0] == null) {
                continue;
            }
            touristGoods.add((TouristGood) objects[0]);
        }
        return new PageImpl<>(touristGoods, pageable, page.getTotalElements());
    }

    @Override
    public TouristGood saveTouristGood(TouristSupplier touristSupplier, Long id, String touristName, ActivityType activityType, TouristType touristType
            , String touristFeatures, Address destination, Address placeOfDeparture, Address travelledAddress
            , BigDecimal price, BigDecimal childrenDiscount, BigDecimal rebate, String receptionPerson
            , String receptionTelephone, String eventDetails, String beCareful, String touristImgUri
            , int maxPeople, Long mallProductId, List<String> photos, TouristCheckStateEnum goodsCheckState) {
        TouristGood touristGood = null;
        if (id != null) {
            touristGood = touristGoodRepository.getOne(id);
            touristGood.setCreateTime(LocalDateTime.now());
        } else {
            touristGood = new TouristGood();
            touristGood.setUpdateTime(LocalDateTime.now());
        }
        touristGood.setTouristSupplier(touristSupplier);
        touristGood.setMallProductId(mallProductId);
        touristGood.setTouristName(touristName);
        touristGood.setActivityType(activityType);
        touristGood.setTouristType(touristType);
        touristGood.setTouristFeatures(touristFeatures);
        touristGood.setDestination(destination);
        touristGood.setPlaceOfDeparture(placeOfDeparture);
        touristGood.setTravelledAddress(travelledAddress);
        touristGood.setPrice(price);
        touristGood.setChildrenDiscount(childrenDiscount);
        touristGood.setRebate(rebate);
        touristGood.setReceptionPerson(receptionPerson);
        touristGood.setReceptionTelephone(receptionTelephone);
        touristGood.setEventDetails(eventDetails);
        touristGood.setBeCareful(beCareful);
        touristGood.setTouristImgUri(touristImgUri);
        touristGood.setMaxPeople(maxPeople);
        touristGood.setImages(photos);
        touristGood.setTouristCheckState(goodsCheckState);
        return touristGoodRepository.saveAndFlush(touristGood);
    }

    @Override
    public List<TouristGood> findNewTourists(int offset) {
        int pageNo = (int) Math.ceil(offset / 10F);
        Pageable pageable = new PageRequest(pageNo, 10, new Sort(Sort.Direction.DESC, "updateTime"));
        Page<TouristGood> page = touristGoodRepository.findAll((root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("deleted").as(Boolean.class), false);
            predicate = cb.and(predicate, cb.equal(root.get("touristCheckState").as(TouristCheckStateEnum.class),
                    TouristCheckStateEnum.CheckFinish));
            return predicate;
        }, pageable);
        return page.getContent();
    }



    @Override
    public List<Address> findByDestinationTown() {
        List<Address> list = touristGoodRepository.findByDestinationTown(false, TouristCheckStateEnum.CheckFinish);
        return list;
//        return touristGoodRepository.findAll((root, query, cb) -> {
//            Predicate predicate = cb.equal(root.get("deleted").as(Boolean.class), false);
//            predicate = cb.and(predicate, cb.equal(root.get("touristCheckState").as(TouristCheckStateEnum.class),
//                    TouristCheckStateEnum.CheckFinish));
//            query = query.where(predicate);
//            query = query.groupBy(root.get("destination").get("town"));
//            return query.getRestriction();
//        });
    }

    @Override
    public List<TouristGood> findByMddxlTourist(String[] cityNames, Integer[] sorts, Long[] activityIds
            , Long[] touristTypeIds, int offset) {
        int pageNo = (int) Math.ceil(offset / 10F);
        Sort sort = null;
        if (sorts != null) {
            List list = Arrays.asList(sorts);
            if (list.contains(1) && list.contains(0)) {
                sort = new Sort(Sort.Direction.DESC, "updateTime", "rebate");
            } else if (list.contains(1)) {
                sort = new Sort(Sort.Direction.DESC, "rebate");
            } else {
                sort = new Sort(Sort.Direction.DESC, "updateTime");
            }
        }
        Pageable pageable = new PageRequest(pageNo, 10, sort);
        Page<TouristGood> page = touristGoodRepository.findAll((root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("deleted").as(Boolean.class), false);
            predicate = cb.and(predicate, cb.equal(root.get("touristCheckState").as(TouristCheckStateEnum.class),
                    TouristCheckStateEnum.CheckFinish));
            if (activityIds != null && activityIds.length > 0)
                predicate = cb.and(predicate, root.get("activityType").get("id").in(activityIds));
            if (touristTypeIds != null && touristTypeIds.length > 0)
                predicate = cb.and(predicate, root.get("touristType").get("id").in(touristTypeIds));
            if (cityNames != null && cityNames.length > 0)
                predicate = cb.and(predicate, root.get("destination").get("town").in(cityNames));
            return predicate;
        }, pageable);
        return page.getContent();
    }


}
