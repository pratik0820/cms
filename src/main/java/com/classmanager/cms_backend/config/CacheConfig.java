package com.classmanager.cms_backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(defaultCacheSpec());
        return manager;
    }


    @Bean("shortLivedCacheManager")
    public CacheManager shortLivedCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());
        return manager;
    }

    @Bean("longLivedCacheManager")
    public CacheManager longLivedCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats());
        return manager;
    }

    private Caffeine<Object, Object> defaultCacheSpec() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats();
    }

    // Cache name constants — use these instead of raw strings
    public static final String CACHE_TENANTS     = "tenants";
    public static final String CACHE_TIMETABLE   = "timetable";
    public static final String CACHE_SYLLABUS    = "syllabus";
    public static final String CACHE_BRANCHES    = "branchList";
    public static final String CACHE_USER_DETAILS = "userDetails";
}
