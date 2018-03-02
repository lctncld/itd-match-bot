# Match Bot for Telegram

## Overview

One day, one day...

## Technologies

- Java 8
- Spring Boot 2
- Spring WebFlux (Netty web server)
- Redis
- [java-telegram-bot-api](https://github.com/pengrad/java-telegram-bot-api)
- [ngrok](https://ngrok.com/)

## Database Structure

Redis KEYs:
- users (HASH user_id -> ...profile params)
- locations (SORTEDSET lat, lon -> user_id)
- session (HASH user_id -> current_step)

## Development guidelines

Commit to `dev` or feature branches, commits to `master` trigger deployment to Heroku and are off limits.

## Local Development

- Install ngrok from website or, on a mac, with `brew cask install ngrok`.
- Expose port 8080 with `ngrok http 8080`
- Overwrite these props in `application.yml` or set them
[some other way](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).
```
telegram:
  token: so-secret-much-token
  webhook_url: https://...ngrok.io

```
- Grab and start redis:
```
docker run --name itd-redis -p 6379:6379 -d redis:4
```

To examine its contents directly, use
```
docker exec -it itd-redis redis-cli
```

- Run application and try to ping your bot from Telegram
