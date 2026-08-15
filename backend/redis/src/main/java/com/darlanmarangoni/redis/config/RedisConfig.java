package com.darlanmarangoni.redis.config;

import com.darlanmarangoni.redis.example.Greeting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Greeting> greetingRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        JacksonJsonRedisSerializer<Greeting> valueSerializer = new JacksonJsonRedisSerializer<>(Greeting.class);

        RedisSerializationContext<String, Greeting> context = RedisSerializationContext
                .<String, Greeting>newSerializationContext(new StringRedisSerializer())
                .value(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
