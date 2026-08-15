package com.darlanmarangoni.redis.transacao;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class TransacaoService {

    private static final String KEY_PREFIX = "transacao:";
    private static final Duration TTL = Duration.ofHours(24);

    private final ReactiveRedisTemplate<String, Transacao> redisTemplate;

    public TransacaoService(ReactiveRedisTemplate<String, Transacao> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Transacao> salvar(Transacao transacao) {
        return redisTemplate.opsForValue()
                .set(KEY_PREFIX + transacao.id(), transacao, TTL)
                .thenReturn(transacao);
    }

    public Mono<Transacao> buscarPorId(String id) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + id);
    }
}
