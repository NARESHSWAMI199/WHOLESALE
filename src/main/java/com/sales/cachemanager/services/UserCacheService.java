package com.sales.cachemanager.services;


import com.sales.cachemanager.RedisConstants;
import com.sales.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCacheService {

    private final CacheManager cacheManager;
    
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(UserCacheService.class);

    public User getCacheUser(String slug){
        Cache cache = cacheManager.getCache(RedisConstants.USER_REDIS_CACHE_NAME);
        logger.debug("Getting user from redis : {}",slug);
        return cache == null ?  null : cache.get(slug,User.class);
    }


    @Cacheable(value = RedisConstants.USER_REDIS_CACHE_NAME, key="#user.slug")
    public User getOrWriteCacheUser(User user){
        return user;
    }

    @CachePut(value = RedisConstants.USER_REDIS_CACHE_NAME, key = "#user.slug")
    public User saveCacheUser(User user){
        logger.debug("Saving a user in cache : {}", Objects.nonNull(user) ? user.getSlug() :null);
        return user;
    }

    @CacheEvict(value = RedisConstants.USER_REDIS_CACHE_NAME, key = "#slug")
    public void evictCacheUser(String slug){
        logger.info("User delete from redis : {}",slug);
    }


}
