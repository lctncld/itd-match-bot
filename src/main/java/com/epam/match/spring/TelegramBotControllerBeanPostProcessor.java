package com.epam.match.spring;

import com.epam.match.service.session.ProfileSetupStep;
import com.epam.match.spring.annotation.MessageMapping;
import com.epam.match.spring.annotation.TelegramBotController;
import com.epam.match.spring.registry.TelegramBotHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TelegramBotControllerBeanPostProcessor implements BeanPostProcessor {

  @Autowired
  private TelegramBotHandlerRegistry registry;

  private final Map<String, Class> controllers = new HashMap<>();

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    Class<?> beanClass = bean.getClass();
    if (beanClass.isAnnotationPresent(TelegramBotController.class)) {
      log.info("Found bot controller {}:{}", beanName, beanClass);
      controllers.put(beanName, beanClass);
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (controllers.containsKey(beanName)) {
      Class original = controllers.get(beanName);
      Method[] methods = original.getMethods();
      Arrays.stream(methods)
        .filter(method -> method.isAnnotationPresent(MessageMapping.class))
        .peek(method -> log.info("Fount bot controller method #{}", method))
        .forEach(method -> {
          MessageMapping mapping = method.getAnnotation(MessageMapping.class);
          String command = mapping.value();
          if ("".equals(command)) {
            ProfileSetupStep step = mapping.step();
            if (step != ProfileSetupStep.UNKNOWN) {
              command = step.toString();
            }
          }
          log.info("Found handler {} : {}", command, method);
          HandlerMethod handler = new HandlerMethod(bean, method);
          registry.addMessageHandler(command, handler);
        });
    }
    return bean;
  }
}
