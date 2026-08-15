package com.darlanmarangoni.redis.transacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveZSetOperations<String, String> zSetOperations;

    private MovimentacaoService movimentacaoService;

    @BeforeEach
    void setUp() {
        movimentacaoService = new MovimentacaoService(redisTemplate);
    }

    @Test
    void registrarDeveGravarMovimentacaoDeEnvioNaContaOrigemEDeRecebimentoNaContaDestino() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.add(any(), any(), anyDouble())).thenReturn(Mono.just(true));
        when(zSetOperations.removeRangeByScore(any(), any())).thenReturn(Mono.just(0L));
        when(redisTemplate.expire(any(), any(Duration.class))).thenReturn(Mono.just(true));

        Transacao transacao = criarTransacao("tx-1", new BigDecimal("150.00"));

        StepVerifier.create(movimentacaoService.registrar(transacao))
                .verifyComplete();

        ArgumentCaptor<String> chaveCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> membroCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(zSetOperations, Mockito.times(2))
                .add(chaveCaptor.capture(), membroCaptor.capture(), anyDouble());

        assertThat(chaveCaptor.getAllValues()).containsExactlyInAnyOrder(
                "movimentacao:111:0001:12345-6",
                "movimentacao:222:0002:98765-4");
        assertThat(membroCaptor.getAllValues())
                .anySatisfy(membro -> assertThat(membro).isEqualTo("ENVIADO:tx-1:150.00"));
        assertThat(membroCaptor.getAllValues())
                .anySatisfy(membro -> assertThat(membro).isEqualTo("RECEBIDO:tx-1:150.00"));
    }

    @Test
    void resumoDeveSomarEnviadoERecebidoEmCadaJanela() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.rangeByScore(eq("movimentacao:111:0001:12345-6"), any()))
                .thenReturn(Flux.just(
                        "ENVIADO:tx-1:100.00",
                        "ENVIADO:tx-2:50.00",
                        "RECEBIDO:tx-3:30.00"));

        StepVerifier.create(movimentacaoService.resumo("111", "0001", "12345-6"))
                .assertNext(resumo -> {
                    assertThat(resumo.ultimos2Minutos().enviado()).isEqualByComparingTo("150.00");
                    assertThat(resumo.ultimos2Minutos().recebido()).isEqualByComparingTo("30.00");
                    assertThat(resumo.ultimos2Minutos().total()).isEqualByComparingTo("180.00");
                    assertThat(resumo.ultimos5Minutos().total()).isEqualByComparingTo("180.00");
                    assertThat(resumo.ultimaHora().total()).isEqualByComparingTo("180.00");
                })
                .verifyComplete();
    }

    @Test
    void resumoSemMovimentacoesDeveRetornarZeroEmTodasAsJanelas() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.rangeByScore(any(), any())).thenReturn(Flux.empty());

        StepVerifier.create(movimentacaoService.resumo("999", "0000", "0"))
                .assertNext(resumo -> {
                    assertThat(resumo.ultimos2Minutos().total()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(resumo.ultimos5Minutos().total()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(resumo.ultimaHora().total()).isEqualByComparingTo(BigDecimal.ZERO);
                })
                .verifyComplete();
    }

    private Transacao criarTransacao(String id, BigDecimal valor) {
        Cliente clienteOrigem = new Cliente("Darlan", "111", List.of());
        Cliente clienteDestino = new Cliente("Fulano", "222", List.of());
        Conta contaOrigem = new Conta("0001", "12345-6", List.of());
        Conta contaDestino = new Conta("0002", "98765-4", List.of());

        return new Transacao(id, clienteOrigem, clienteDestino, contaOrigem, contaDestino, valor);
    }
}
