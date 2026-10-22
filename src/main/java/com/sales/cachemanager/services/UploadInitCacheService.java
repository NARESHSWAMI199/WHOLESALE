package com.sales.cachemanager.services;


import com.sales.cachemanager.RedisConstants;
import com.sales.requests.records.UploadInitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadInitCacheService {

    private final CacheManager cacheManager;

    public UploadInitRequest getCacheUploadInitRequest(String uploadId) {
        Cache cache = cacheManager.getCache(RedisConstants.JOB_REDIS_CACHE_NAME);
        log.debug("Getting UploadInitRequest from redis : {}", uploadId);
        return cache == null ? null : cache.get(uploadId, UploadInitRequest.class);
    }


    @CachePut(value = RedisConstants.JOB_REDIS_CACHE_NAME, key = "#uploadId")
    public UploadInitRequest updateUploadInitRequest(String uploadId, UploadInitRequest uploadInitRequest) {
        log.debug("Updating cache for uploadId {}", uploadId);
        return uploadInitRequest;
    }

    @CacheEvict(value = RedisConstants.JOB_REDIS_CACHE_NAME, key = "#uploadId")
    public void deletingUploadInitRequest(String uploadId) {
        log.debug("Deleting cache for job {}", uploadId);
    }

}
