package com.epam.match.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

  @Value("${REDIS_URL}")
  private String redisUrl;

  @Bean
  public RedisClient redisClient() {
    return RedisClient.create(redisUrl);
  }

  @Bean
  public RedisReactiveCommands<String, String> commands(RedisClient redisClient) {
    return redisClient.connect().reactive();
  }
}
