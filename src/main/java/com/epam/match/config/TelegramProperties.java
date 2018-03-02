package com.epam.match.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("telegram")
@Data
public class TelegramProperties {

  private String token;

  private String webHookUrl;
}
