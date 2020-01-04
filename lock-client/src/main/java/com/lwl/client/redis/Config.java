package com.lwl.client.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 9:09
 */
@Configuration
@Slf4j
public class Config {
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${redisson.address}")
    private String address;
    @Value("${spring.redis.database}")
    private String database;
    @Value("${spring.redis.password}")
    private String password;

    @Primary
    @Bean
    public RedisTemplate redisTemplate() {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedissonClient redissonClient(){
        log.info("----正在初始化redissonClient---");
        org.redisson.config.Config config = new org.redisson.config.Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setClientName("clientName");
        singleServerConfig.setAddress(address);
        singleServerConfig.setDatabase(Integer.parseInt(database));
        singleServerConfig.setPassword(password);
        return Redisson.create(config);
    }
}
