package com.sales.global;

import com.sales.entities.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalConstant {
    public static String secretKey = "asdfghj123456789";
    public static int suId =1;
    public  static  int groupId = 1;
    public static int OTHER_CATEGORY = 0;
    public static int OTHER_SUBCATEGORY = 0;
    public static final String WHOLESALER_IMAGE_PATH = "/wholesale/auth/profile/";
    public static final String PATH_SEPARATOR = "/";
    public static final String STAFF_IMAGE_PATH = "/admin/auth/profile/";
    public static final String CHAT_STATIC_PATH = "/chat/images/";

    // for normal images
    public static int minWidth = 500;
    public static int minHeight = 500;
    public static int maxWidth = 10024;
    public static int maxHeight = 10024;

    // for banner images
    public static int bannerMinWidth = 500;
    public static int bannerMinHeight = 500;
    public static int bannerMaxWidth = 10000;
    public static int bannerMaxHeight = 10000;

    public static double[] allowedAspectRatios = {1.0, 1.33, 1.78};
    public static String[] allowedFormats = {"jpg", "jpeg", "png", "gif",};
    public static String removeBgUrl = "http://localhost:5000/remove-background";

    public static final Map<String, User> onlineUsers = new ConcurrentHashMap<>();

    public static final String ITEMS_NOT_UPDATED_PATH_FOR_ADMIN = "/admin/item/notUpdated/";
    public static final String ITEMS_NOT_UPDATED_PATH_FOR_WHOLESALE = "/wholesale/item/notUpdated/";
    public static final String AUTH_TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORIZATION = "Authorization";

    public static final List<String> HEADERS_FOR_ITEMS = List.of("NAME","TOKEN","PRICE", "DISCOUNT","LABEL","CAPACITY","RATING","IN-ITEM_STOCK","STATUS","CREATED-AT","UPDATED-AT");
    public static final List<String> HEADERS_NOT_UPDATED_ITEMS_EXCEL = List.of("NAME", "TOKEN", "PRICE", "DISCOUNT", "LABEL", "CAPACITY", "IN-ITEM_STOCK", "REASON");


    private GlobalConstant () {
    }

}
