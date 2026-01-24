package com.sales.wholesaler.repository;


import com.sales.claims.AuthUser;
import com.sales.commons.repositories.CommonHbRepository;
import com.sales.dto.WholesaleItemDto;
import com.sales.entities.*;
import com.sales.requests.ItemRequest;
import com.sales.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Transactional
@RequiredArgsConstructor
public class WholesaleItemHbRepository implements CommonHbRepository {

    private final EntityManager entityManager;

    // find with pageable and specs.
    public Page<WholesaleItemDto> findAll(Specification<Item> spec, Pageable pageable){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        Long itemsCounts = getCounts(entityManager,criteriaBuilder, spec,Item.class);
        if (itemsCounts == 0) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }
        List<WholesaleItemDto> content = getAllFilteredItems(criteriaBuilder,spec, pageable);
        return new PageImpl<>(content, pageable, itemsCounts);
    }

    public List<WholesaleItemDto> getAllFilteredItems(CriteriaBuilder criteriaBuilder, Specification<Item> spec, Pageable pageable) {
        CriteriaQuery<WholesaleItemDto> criteriaQuery = criteriaBuilder.createQuery(WholesaleItemDto.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        criteriaQuery.multiselect(
                root.get(Item_.id),
                root.get(Item_.name),
                root.get(Item_.label),
                root.get(Item_.capacity),
                root.get(Item_.price),
                root.get(Item_.discount),
                root.get(Item_.description),
                root.get(Item_.avtars),
                root.get(Item_.rating),
                root.get(Item_.totalRatingCount),
                root.get(Item_.totalReviews),
                root.get(Item_.totalReportsCount),
                root.get(Item_.status),
                root.get(Item_.createdAt),
                root.get(Item_.slug),
                root.get(Item_.inStock),
                root.get(Item_.wholesaleId),
                criteriaBuilder.coalesce(root.join(Item_.itemCategory, JoinType.LEFT).get(ItemCategory_.category), ""),
                criteriaBuilder.coalesce(root.join(Item_.itemSubCategory, JoinType.LEFT).get(ItemSubCategory_.subcategory), ""),
                criteriaBuilder.coalesce(root.join(Item_.createdBy, JoinType.LEFT).get(User_.USERNAME), "")
        );

        if (spec != null) {
            criteriaQuery.where(spec.toPredicate(root, criteriaQuery, criteriaBuilder));
        }
        // Sorting
        applySorting(criteriaBuilder, criteriaQuery, root, pageable.getSort());
        TypedQuery<WholesaleItemDto> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return query.getResultList();
    }

    public int updateItems(ItemRequest WholesaleItemDto, AuthUser loggedUser){
        String hqQuery = "update Item set " +
                "name =:name," +
                "capacity =:capacity," +
                "description =:description," +
                "label =:label,"+
                "price =:price," +
                "discount =:discount," +
                "itemCategory =:itemCategory,"+
                "itemSubCategory =:itemSubCategory,"+
                "updatedAt =:updatedAt," +
                "updatedBy =:updatedBy " +
                "where slug =:slug and wholesaleId =:wholesaleId";
        Query query = entityManager.createQuery(hqQuery);
        query.setParameter("name" , WholesaleItemDto.getName());
        query.setParameter("capacity" , WholesaleItemDto.getCapacity());
        query.setParameter("description" , WholesaleItemDto.getDescription());
        query.setParameter("label" , WholesaleItemDto.getLabel());
        query.setParameter("price" , WholesaleItemDto.getPrice());
        query.setParameter("discount" , WholesaleItemDto.getDiscount());
        query.setParameter("itemCategory" , WholesaleItemDto.getItemCategory());
        query.setParameter("itemSubCategory" , WholesaleItemDto.getItemSubCategory());
        query.setParameter("updatedAt" , Utils.getCurrentMillis());
        query.setParameter("updatedBy" , loggedUser.getId());
        query.setParameter("slug", WholesaleItemDto.getSlug());
        query.setParameter("wholesaleId", WholesaleItemDto.getStoreId());
        return  query.executeUpdate();
    }


    public int deleteItem(String slug,Integer storeId){
        String hqlString = "update Item set isDeleted='Y' where slug=:slug and wholesaleId =:wholesaleId";
        Query query = entityManager.createQuery(hqlString);
        query.setParameter("slug",slug);
        query.setParameter("wholesaleId",storeId);
        return query.executeUpdate();
    }

    public int updateStock(String stock , String slug, Integer storeId){
        String hqlString = "update Item set inStock=:stock where slug=:slug and wholesaleId =:wholesaleId";
        Query query = entityManager.createQuery(hqlString);
        query.setParameter("stock",stock);
        query.setParameter("slug",slug);
        query.setParameter("wholesaleId",storeId);
        return query.executeUpdate();
    }

    public int updateStatus(String slug, String status){
        String hql = "Update Item set status=:status where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("status",status);
        query.setParameter("slug",slug);
        return query.executeUpdate();
    }
    public int updateItemImages(String slug , String filename){
        String hql = "update Item set avtars =:avtars where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("avtars", filename);
        query.setParameter("slug", slug);
        return query.executeUpdate();
    }


    /// For updateItemsViaExcelSheet

    @Getter
    @Setter
    @ToString
    public static class ItemUpdateError {
        Map<String,Object> itemRowDetail;
        String errorMessage;
    }


    public int updateExcelSheetItems(ItemRequest WholesaleItemDto, Integer userId, Integer wholesaleId){
        String hql = """
           update Item set name=:name,
                label=:label,
                capacity=:capacity,
                price=:price,
                discount=:discount,
                inStock=:inStock,
                updatedAt=:updatedAt,
                updatedBy=:updatedBy
           where slug=:slug and wholesaleId=:wholesaleId
        """;
        Query query = entityManager.createQuery(hql);
        query.setParameter("name", WholesaleItemDto.getName())
                .setParameter("label", WholesaleItemDto.getLabel())
                .setParameter("capacity", WholesaleItemDto.getCapacity())
                .setParameter("price", WholesaleItemDto.getPrice())
                .setParameter("discount", WholesaleItemDto.getDiscount())
                .setParameter("inStock", WholesaleItemDto.getInStock())
                .setParameter("updatedAt", Utils.getCurrentMillis())
                .setParameter("updatedBy", userId)
                .setParameter("slug", WholesaleItemDto.getSlug())
                .setParameter("wholesaleId",wholesaleId);
        return query.executeUpdate();
    }



}
