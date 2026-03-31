package com.sales.wholesaler.services;


import com.sales.admin.repositories.PaginationRepository;
import com.sales.claims.AuthUser;
import com.sales.entities.Pagination;
import com.sales.entities.User;
import com.sales.entities.UserPagination;
import com.sales.global.ResponseMessages;
import com.sales.global.USER_TYPES;
import com.sales.request.UserPaginationRequest;
import com.sales.specifications.PaginationSpecification;
import com.sales.utils.Utils;
import com.sales.wholesaler.mapper.WholesaleUserPaginationMapper;
import com.sales.wholesaler.repository.WholesalePaginationHbRepository;
import com.sales.wholesaler.repository.WholesalePaginationRepository;
import com.sales.wholesaler.repository.WholesaleUserPaginationsRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WholesalePaginationService {

    private final WholesaleUserPaginationsRepository wholesaleUserPaginationsRepository;
    private final WholesalePaginationRepository wholesalePaginationRepository;
    private final WholesalePaginationHbRepository wholesalePaginationHbRepository;
    private final PaginationRepository paginationRepository;
    private final WholesaleUserPaginationMapper wholesaleUserPaginationMapper;

    public List<UserPagination> findAllUserPagination() {
        return wholesaleUserPaginationsRepository.findAll();
    }

    public Map<String, Object> findUserPaginationByUserId(AuthUser loggedUser) {
        List<UserPagination> userPaginationList = wholesaleUserPaginationsRepository.getUserPaginationByUserId(loggedUser.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        for (UserPagination userPagination : userPaginationList) {
            Pagination pagination = userPagination.getPagination();
            String key = pagination.getFieldFor();
            // remove all whitespaces and changed with uppercase like:
            // abc d → ABCD
            key = key.replaceAll("\\s+", "").toUpperCase();
            result.put(key, wholesaleUserPaginationMapper.toDto(userPagination));
        }
        return result;
    }


    public Pagination findByFieldFor(String fieldsFor) {
        return wholesalePaginationRepository.findByFieldFor(fieldsFor);
    }


    @Transactional(rollbackFor = {InternalException.class, RuntimeException.class, Exception.class})
    public void setUserDefaultPaginationForSettings(User user) {
        Specification<Pagination> specification = Specification.allOf(PaginationSpecification.whoCanSee("B")
                .or(PaginationSpecification.whoCanSee(USER_TYPES.WHOLESALER.getType()))
        );
        List<Pagination> allPagination = wholesalePaginationRepository.findAll(specification);
        for (Pagination pagination : allPagination) {
            UserPagination userPagination = insertUserPagination(pagination, user, 25); // default rows are 25
            if (userPagination == null)
                throw new InternalException(ResponseMessages.WE_ARE_UNABLE_TO_SAVE_YOUR_DEFAULT_PAGINATION_SETTINGS);

        }
    }

    @Transactional(rollbackFor = {InternalException.class, RuntimeException.class, Exception.class})
    public UserPagination insertUserPagination(Pagination pagination, AuthUser loggedUser, Integer rowNumbers) {
        UserPagination userPagination = new UserPagination();
        Pagination savedPagination = paginationRepository.save(pagination);
        userPagination.setPagination(savedPagination);
        userPagination.setUserId(loggedUser.getId());
        userPagination.setRowsNumber(rowNumbers);
        return wholesaleUserPaginationsRepository.save(userPagination);
    }


    public int updateUserPaginationRowsNumber(UserPaginationRequest userPaginationRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // Check required fields are not null
        Utils.checkRequiredFields(userPaginationRequest, List.of("paginationId", "rowsNumber"));
        // check pagination field available or not
        return wholesalePaginationHbRepository.updateUserPaginations(userPaginationRequest, loggedUser);
    }

}
