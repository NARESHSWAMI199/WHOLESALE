package com.sales.dao;

import com.sales.claims.AuthUser;
import com.sales.dto.SearchFilters;
import com.sales.entities.Item;
import com.sales.requests.ItemRequest;
import org.springframework.data.domain.Page;

public interface ItemsDao {

    Page<Item> getAllItems(SearchFilters searchFilters);

    int updateItem(ItemRequest itemRequest, AuthUser loggedUser);

    Item createItem(ItemRequest itemRequest, AuthUser loggedUser) throws Exception;

    Item findItemBySLug(String slug);

    int deleteItem(String slug);

    int updateStock(String stock, String slug);


}
