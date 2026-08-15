package com.darlanmarangoni.redis.transacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransacaoControllerTest {

    @Mock
    private TransacaoService transacaoService;

    @Mock
    private MovimentacaoService movimentacaoService;

    private TransacaoController transacaoController;

    @BeforeEach
    void setUp() {
        transacaoController = new TransacaoController(transacaoService, movimentacaoService);
    }

    @Test
    void receberDeveGerarIdSalvarERegistrarMovimentacaoRetornando201() {
        when(transacaoService.salvar(any())).thenAnswer(invocacao -> Mono.just(invocacao.getArgument(0)));
        when(movimentacaoService.registrar(any())).thenReturn(Mono.empty());

        Transacao entrada = criarTransacao(null, new BigDecimal("150.00"));

        StepVerifier.create(transacaoController.receber(entrada))
                .assertNext(resposta -> {
                    assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                    Transacao corpo = resposta.getBody();
                    assertThat(corpo).isNotNull();
                    assertThat(corpo.id()).isNotBlank();
                    assertThat(corpo.clienteOrigem()).isEqualTo(entrada.clienteOrigem());
                    assertThat(corpo.clienteDestino()).isEqualTo(entrada.clienteDestino());
                    assertThat(corpo.contaOrigem()).isEqualTo(entrada.contaOrigem());
                    assertThat(corpo.contaDestino()).isEqualTo(entrada.contaDestino());
                    assertThat(corpo.valor()).isEqualByComparingTo(entrada.valor());
                })
                .verifyComplete();

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        Mockito.verify(movimentacaoService).registrar(captor.capture());
        assertThat(captor.getValue().id()).isNotBlank();
    }

    @Test
    void buscarDeveRetornar200ComATransacaoQuandoEncontrada() {
        Transacao transacao = criarTransacao("tx-1", new BigDecimal("100.00"));
        when(transacaoService.buscarPorId(eq("tx-1"))).thenReturn(Mono.just(transacao));

        StepVerifier.create(transacaoController.buscar("tx-1"))
                .assertNext(resposta -> {
                    assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(resposta.getBody()).isEqualTo(transacao);
                })
                .verifyComplete();
    }

    @Test
    void buscarDeveRetornar404QuandoTransacaoNaoExiste() {
        when(transacaoService.buscarPorId(eq("nao-existe"))).thenReturn(Mono.empty());

        StepVerifier.create(transacaoController.buscar("nao-existe"))
                .assertNext(resposta -> assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
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
