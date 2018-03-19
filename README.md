# Match Bot for Telegram

## Overview

One day, one day...

## Technologies

- Java 9
- Spring Boot
- Reactor
- Redis, with [Lettuce](https://lettuce.io/core/release/reference/)
- [java-telegram-bot-api](https://github.com/pengrad/java-telegram-bot-api)
- [ngrok](https://ngrok.com/)

## Database Structure

Redis KEYs:

- user_id:contact:
    - first_name
    - last_name
    - chat_id
    - phone
- user_id:profile:
    - age
    - gender
    - match_gender
    - match_min_age
    - match_max_age
- user_id:image
- user_id:seen
- user_id:likes
- user_id:dislikes
- user_id:session
- locations

## Development guidelines

Commit to `dev` or feature branches, commits to `master` trigger deployment to Heroku and are off limits.

## Local Development

- Install ngrok from website or, on a mac, with `brew cask install ngrok`.
- Expose port 8080 with `ngrok http 8080`
- Add these props to `application-dev.yml` or set them
[some other way](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).
```
TELEGRAM_TOKEN: so-secret-much-token-generate-it-yourself
TELEGRAM_WEBHOOK_URL: https://...ngrok.io
REDIS_URL: redis://localhost:6379
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

- To show demo from the phone:
```
adb devices
adb shell screenrecord --size=720x1440 --output-format=h264 - | mpv - --fps=30 --untimed --framedrop=no
```