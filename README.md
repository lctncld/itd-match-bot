# Match Bot for Telegram

## Overview

- Java 8
- Spring Boot 2
- Spring WebFlux
- [java-telegram-bot-api](https://github.com/pengrad/java-telegram-bot-api)
- ngrok

## Environment

Populate `application.yml`:

```
telegram:
  token: your-telegram-bot-token
  webhook:
    url: your-domain-with-https

```