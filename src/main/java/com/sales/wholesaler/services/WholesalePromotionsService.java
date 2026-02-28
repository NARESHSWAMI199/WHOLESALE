package com.sales.wholesaler.services;


import com.sales.claims.AuthUser;
import com.sales.request.StorePromotionDto;
import com.sales.global.ConstantResponseKeys;
import com.sales.wholesaler.repository.WholesaleHbPromotionRepository;
import com.sales.wholesaler.repository.WholesaleStoreRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WholesalePromotionsService  {

    private final WholesaleStoreRepository wholesaleStoreRepository;
    private final WholesaleHbPromotionRepository wholesaleHbPromotionRepository;
    private static final Logger logger = LoggerFactory.getLogger(WholesalePromotionsService.class);

    public Map<String,Object> insertItemPromotion(StorePromotionDto storePromotionDto, AuthUser loggedUser) {
        logger.debug("Starting insertItemPromotion method with storePromotionDto: {}, loggedUser: {}", storePromotionDto, loggedUser);
        Map<String,Object> response = new HashMap<>();
        Integer storeId = wholesaleStoreRepository.getStoreIdByUserId(loggedUser.getId());
        storePromotionDto.setStoreId(storeId);
        int isInserted = wholesaleHbPromotionRepository.insertStorePromotions(storePromotionDto,loggedUser); // Create operation
        if(isInserted > 0){
            response.put(ConstantResponseKeys.MESSAGE,"Your item is going to promote.");
            response.put(ConstantResponseKeys.STATUS,200);
        }else {
            response.put(ConstantResponseKeys.MESSAGE,"Something went wrong during promote item. if your money was deducted contact to administrator");
            response.put(ConstantResponseKeys.STATUS,400);
        }
        logger.debug("Completed insertItemPromotion method");
        return response;
    }


}
