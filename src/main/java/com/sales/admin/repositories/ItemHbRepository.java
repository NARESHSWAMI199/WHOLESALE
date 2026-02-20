package com.sales.admin.repositories;


import com.sales.claims.AuthUser;
import com.sales.entities.User;
import com.sales.requests.ItemRequest;
import com.sales.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Transactional
@RequiredArgsConstructor
public class ItemHbRepository{

    private final EntityManager entityManager;

    public int updateItems(ItemRequest itemRequest, AuthUser loggedUser){
        String hqQuery = "update Item set " +
                "name =:name," +
                "capacity =:capacity," +
                "description =:description," +
                "price =:price," +
                "discount =:discount," +
                "itemCategory =:itemCategory,"+
                "itemSubCategory =:itemSubCategory,"+
                "updatedAt =:updatedAt," +
                "updatedBy =:updatedBy " +
                "where slug =:slug ";
        Query query = entityManager.createQuery(hqQuery);
        query.setParameter("name" , itemRequest.getName());
        query.setParameter("capacity" , itemRequest.getCapacity());
        query.setParameter("description" , itemRequest.getDescription());
        query.setParameter("price" , itemRequest.getPrice());
        query.setParameter("discount" , itemRequest.getDiscount());
        query.setParameter("itemCategory" , itemRequest.getItemCategory());
        query.setParameter("itemSubCategory" , itemRequest.getItemSubCategory());
        query.setParameter("updatedAt" , Utils.getCurrentMillis());
        query.setParameter("updatedBy" ,  User.builder().id(loggedUser.getId()).build());
        query.setParameter("slug", itemRequest.getSlug());
        return  query.executeUpdate();
    }


    public int deleteItem(String slug){
        String hqlString = "update Item set isDeleted='Y' where slug=:slug";
        Query query = entityManager.createQuery(hqlString);
        query.setParameter("slug",slug);
        return query.executeUpdate();
    }

    public int updateStock(String stock , String slug){
        String hqlString = "update Item set inStock=:stock where slug=:slug";
        Query query = entityManager.createQuery(hqlString);
        query.setParameter("stock",stock);
        query.setParameter("slug",slug);
        return query.executeUpdate();
    }

    public int updateStatus(String slug, String status){
        String hql = "Update Item set status=:status where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("status",status);
        query.setParameter("slug",slug);
        return query.executeUpdate();
    }

    public int insertItemsList(Map<String,List<String>> itemsData,int userId,int wholesaleId){
        List<String> nameList = itemsData.get("NAME");
        List<String> labelList = itemsData.get("LABEL");
        List<String> priceList = itemsData.get("PRICE");
        List<String> discountlist = itemsData.get("DISCOUNT");
        List<String> descriptionList = itemsData.get("DESCRIPTION");
        List<String> avatarList = itemsData.get("AVATAR");
        List<String> in_stockList = itemsData.get("STOCK");

        StringBuilder dataList = new StringBuilder();
        for (int i = 0; i < nameList.size(); i++){
            String label = labelList.get(i).isEmpty() ? "N" : (String) labelList.get(i);
            String in_stock = in_stockList.get(i).isEmpty() ? "N" : (String) in_stockList.get(i);
            String discount = discountlist.get(i).isEmpty() ? "0" : (String) discountlist.get(i);
            String status = "A";
            int price = 0;
            if (priceList.get(i).isEmpty()){
                status = "D";
            }else {
                price = Integer.parseInt((String) priceList.get(i));
            }
            dataList.append("(" + "'").append(nameList.get(i)).append("',").append(wholesaleId).append(",").append("'")
                    .append(label).append("',").append(price).append(",").append(discount).append(",").append("'")
                    .append(descriptionList.get(i)).append("',").append("'").append(avatarList.get(i)).append("',")
                    .append("'0',").append("'").append(status).append("',").append("'N',").append(Utils.getCurrentMillis())
                    .append(",").append(userId).append(",").append(Utils.getCurrentMillis()).append(",").append(userId).append(",")
                    .append("'").append(UUID.randomUUID()).append("',").append("'").append(in_stock).append("'").append(")");
            if (i != nameList.size()-1) dataList.append(",");
        }

        String qs = """
                insert into items (
                    name,
                    store_id,
                    label,
                    price,
                    discount,
                    description,
                    avatar,
                    rating,
                    status,
                    is_deleted,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    slug,
                    in_stock
                ) values  dataList 
                """;

        Query query = entityManager.createNativeQuery(qs);
        return  query.executeUpdate();
    }






    /// For updateItemsViaExcelSheet

    @Getter
    @Setter
    @ToString
    public static class ItemUpdateError {
        Map<String,Object> itemRowDetail;
        String errorMessage;
    }


    public int updateExcelSheetItems(ItemRequest itemRequest, Integer userId, Integer wholesaleId){
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
        query.setParameter("name", itemRequest.getName())
                .setParameter("label", itemRequest.getLabel())
                .setParameter("capacity", itemRequest.getCapacity())
                .setParameter("price", itemRequest.getPrice())
                .setParameter("discount", itemRequest.getDiscount())
                .setParameter("inStock", itemRequest.getInStock())
                .setParameter("updatedAt", Utils.getCurrentMillis())
                .setParameter("updatedBy",  User.builder().id(userId).build())
                .setParameter("slug", itemRequest.getSlug())
                .setParameter("wholesaleId",wholesaleId);
        return query.executeUpdate();
    }


    public int updateItemImage(String slug , String filenames){
        String hql = "update Item set avtars =:avtars where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("avtars", filenames);
        query.setParameter("slug", slug);
        return query.executeUpdate();
    }


    public int deleteItemCategory(String slug){
        String hqlString = "update ItemCategory set isDeleted='Y' where slug=:slug";
        Query query = entityManager.createQuery(hqlString);
        query.setParameter("slug",slug);
        return query.executeUpdate();
    }


    public int deleteItemSubCategory(String slug){
        String hqlString = "update ItemSubCategory set isDeleted='Y' where slug=:slug";
        Query query = entityManager.createQuery(hqlString);
        query.setParameter("slug",slug);
        return query.executeUpdate();
    }

    public int getItemCategoryIdBySlug(String slug){
        String hql = "select id from ItemCategory where slug=:slug ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("slug",slug);
        return (Integer) query.getSingleResult();
    }

    public int switchCategoryToOther(int categoryId){
        String sql = "Update items set category=0 , subcategory=0 where category=:categoryId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("categoryId",categoryId);
        return query.executeUpdate();
    }

    public int switchSubCategoryToOther(int subcategoryId){
        String sql = "Update items set category=0 , subcategory=0 where subcategory=:subcategoryId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("subcategoryId",subcategoryId);
        return query.executeUpdate();
    }



}
