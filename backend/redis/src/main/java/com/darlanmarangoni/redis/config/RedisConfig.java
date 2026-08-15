package com.darlanmarangoni.redis.config;

import com.darlanmarangoni.redis.transacao.Transacao;
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
    public ReactiveRedisTemplate<String, Transacao> transacaoRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        JacksonJsonRedisSerializer<Transacao> valueSerializer = new JacksonJsonRedisSerializer<>(Transacao.class);

        RedisSerializationContext<String, Transacao> context = RedisSerializationContext
                .<String, Transacao>newSerializationContext(new StringRedisSerializer())
                .value(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
