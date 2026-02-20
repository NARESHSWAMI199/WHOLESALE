package com.sales.admin.services;


import com.sales.admin.repositories.PaginationHbRepository;
import com.sales.admin.repositories.PaginationRepository;
import com.sales.admin.repositories.UserPaginationsRepository;
import com.sales.claims.AuthUser;
import com.sales.dto.UserPaginationRequest;
import com.sales.entities.Pagination;
import com.sales.entities.User;
import com.sales.entities.UserPagination;
import com.sales.specifications.PaginationSpecification;
import com.sales.utils.Utils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaginationService {

    private final UserPaginationsRepository userPaginationsRepository;
    private final PaginationRepository paginationRepository;
    private final PaginationHbRepository paginationHbRepository;

    public List<UserPagination> findAllUserPaginations(){
        return userPaginationsRepository.findAll();
    }

    public Map<String,Object> findUserPaginationsByUserId(AuthUser loggedUser){
        List<UserPagination> userPaginations = userPaginationsRepository.getUserPaginationByUserId(loggedUser.getId());
        Map<String,Object> result = new LinkedHashMap<>();
        for(UserPagination userPagination : userPaginations) {
            Pagination pagination = userPagination.getPagination();
            String key = pagination.getFieldFor();
            // remove all whitespaces and changed with uppercase like:
            // abc d → ABCD
            key = key.replaceAll("\\s+", "").toUpperCase();
            result.put(key,userPagination);
        }
        return result;
    }


    public Pagination findByFieldFor(String fieldsFor){
        return paginationRepository.findByFieldFor(fieldsFor);
    }

    @Transactional(rollbackFor = {InternalException.class, RuntimeException.class,Exception.class })
    public void setUserDefaultPaginationForSettings(User user) {
        Specification<Pagination> specification = Specification.allOf(PaginationSpecification.whoCanSee("B")
                .or(PaginationSpecification.whoCanSee(user.getUserType()))
        );
        List<Pagination> allPagination = paginationRepository.findAll(specification);
        for (Pagination pagination : allPagination) {
            UserPagination userPagination = insertUserPagination(pagination,user, 25); // default rows are 25
            if(userPagination == null) throw new InternalException("We are unable to save your default pagination settings.");

        }
    }

    @Transactional(rollbackFor = {InternalException.class, RuntimeException.class,Exception.class })
    public UserPagination insertUserPagination(Pagination pagination,AuthUser loggedUser,Integer rowNumbers) {
        UserPagination userPagination = new UserPagination();
        Pagination savedPagination = paginationRepository.save(pagination);
        userPagination.setPagination(savedPagination);
        userPagination.setUserId(loggedUser.getId());
        userPagination.setRowsNumber(rowNumbers);
        return userPaginationsRepository.save(userPagination);
    }


    public int updateUserPaginationRowsNumber(UserPaginationRequest userPaginationRequest,AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // Check required fields are not null
        Utils.checkRequiredFields(userPaginationRequest,List.of("paginationId","userId"));
        return paginationHbRepository.updateUserPaginations(userPaginationRequest,loggedUser);
    }

}
