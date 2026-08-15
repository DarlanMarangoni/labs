package com.darlanmarangoni.redis.example;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class GreetingService {

    private static final String KEY_PREFIX = "greeting:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final ReactiveRedisTemplate<String, Greeting> redisTemplate;

    public GreetingService(ReactiveRedisTemplate<String, Greeting> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> save(Greeting greeting) {
        return redisTemplate.opsForValue().set(KEY_PREFIX + greeting.id(), greeting, TTL);
    }

    public Mono<Greeting> findById(String id) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + id);
    }

    public Mono<Boolean> deleteById(String id) {
        return redisTemplate.opsForValue().delete(KEY_PREFIX + id);
    }
}
