package com.osamah.games.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10));

        Map<String, RedisCacheConfiguration> customConfigs = new HashMap<>();

        customConfigs.put("gameDetails", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)));

        customConfigs.put("gamesDefaultSearch", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(12)));

        customConfigs.put("userDefaultList", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15)));

        customConfigs.put("userStats", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(customConfigs)
                .build();
    }
}