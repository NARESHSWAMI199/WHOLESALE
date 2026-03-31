package com.sales.wholesaler.services;


import com.sales.claims.AuthUser;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.ResponseMessages;
import com.sales.request.StorePromotionRequest;
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
public class WholesalePromotionsService {

    private static final Logger logger = LoggerFactory.getLogger(WholesalePromotionsService.class);
    private final WholesaleStoreRepository wholesaleStoreRepository;
    private final WholesaleHbPromotionRepository wholesaleHbPromotionRepository;

    public Map<String, Object> insertItemPromotion(StorePromotionRequest storePromotionRequest, AuthUser loggedUser) {
        logger.debug("Starting insertItemPromotion method with storePromotionRequest: {}, loggedUser: {}", storePromotionRequest, loggedUser);
        Map<String, Object> response = new HashMap<>();
        Integer storeId = wholesaleStoreRepository.getStoreIdByUserId(loggedUser.getId());
        storePromotionRequest.setStoreId(storeId);
        int isInserted = wholesaleHbPromotionRepository.insertStorePromotions(storePromotionRequest, loggedUser); // Create operation
        if (isInserted > 0) {
            response.put(ConstantResponseKeys.MESSAGE, ResponseMessages.YOUR_ITEM_IS_GOING_TO_PROMOTE);
            response.put(ConstantResponseKeys.STATUS, 200);
        } else {
            response.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SOMETHING_WENT_WRONG_DURING_PROMOTE_ITEM_IF_YOUR_MONEY_WAS_DEDUCTED_CONTACT_TO_ADMINISTRATOR);
            response.put(ConstantResponseKeys.STATUS, 400);
        }
        logger.debug("Completed insertItemPromotion method");
        return response;
    }


}
