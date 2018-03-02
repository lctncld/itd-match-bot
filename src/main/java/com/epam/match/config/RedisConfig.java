package com.epam.match.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

  @Bean
  public RedisClient redisClient() {
    return RedisClient.create("redis://localhost:6379");
  }

  @Bean
  public RedisReactiveCommands<String, String> commands(RedisClient redisClient) {
    return redisClient.connect().reactive();
  }
}
