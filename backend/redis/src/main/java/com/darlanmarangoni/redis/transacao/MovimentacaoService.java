package com.darlanmarangoni.redis.transacao;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
public class MovimentacaoService {

    private static final String KEY_PREFIX = "movimentacao:";
    private static final Duration JANELA_MAXIMA = Duration.ofHours(1);
    private static final Duration RETENCAO = Duration.ofHours(2);

    private final ReactiveStringRedisTemplate redisTemplate;

    public MovimentacaoService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Void> registrar(Transacao transacao) {
        long agora = System.currentTimeMillis();

        String chaveOrigem = chave(
                transacao.clienteOrigem().documento(),
                transacao.contaOrigem().agencia(),
                transacao.contaOrigem().numero());
        String chaveDestino = chave(
                transacao.clienteDestino().documento(),
                transacao.contaDestino().agencia(),
                transacao.contaDestino().numero());

        return Mono.when(
                adicionar(chaveOrigem, TipoMovimentacao.ENVIADO, transacao, agora),
                adicionar(chaveDestino, TipoMovimentacao.RECEBIDO, transacao, agora));
    }

    public Mono<ResumoMovimentacao> resumo(String documento, String agencia, String numero) {
        String chave = chave(documento, agencia, numero);
        long agora = System.currentTimeMillis();

        return Mono.zip(
                        calcularJanela(chave, agora, Duration.ofMinutes(2)),
                        calcularJanela(chave, agora, Duration.ofMinutes(5)),
                        calcularJanela(chave, agora, Duration.ofHours(1)))
                .map(tupla -> new ResumoMovimentacao(tupla.getT1(), tupla.getT2(), tupla.getT3()));
    }

    private Mono<Void> adicionar(String chave, TipoMovimentacao tipo, Transacao transacao, long agora) {
        String membro = tipo + ":" + transacao.id() + ":" + transacao.valor().toPlainString();
        double limiteAntigo = agora - JANELA_MAXIMA.toMillis();

        return redisTemplate.opsForZSet().add(chave, membro, agora)
                .then(redisTemplate.opsForZSet().removeRangeByScore(chave, Range.closed(Double.NEGATIVE_INFINITY, limiteAntigo)))
                .then(redisTemplate.expire(chave, RETENCAO))
                .then();
    }

    private Mono<JanelaMovimentacao> calcularJanela(String chave, long agora, Duration janela) {
        double minimo = agora - janela.toMillis();

        return redisTemplate.opsForZSet()
                .rangeByScore(chave, Range.closed(minimo, (double) agora))
                .collectList()
                .map(this::somar);
    }

    private JanelaMovimentacao somar(List<String> membros) {
        BigDecimal enviado = BigDecimal.ZERO;
        BigDecimal recebido = BigDecimal.ZERO;

        for (String membro : membros) {
            String[] partes = membro.split(":", 3);
            BigDecimal valor = new BigDecimal(partes[2]);

            if (TipoMovimentacao.ENVIADO.name().equals(partes[0])) {
                enviado = enviado.add(valor);
            } else {
                recebido = recebido.add(valor);
            }
        }

        return new JanelaMovimentacao(enviado, recebido, enviado.add(recebido));
    }

    private String chave(String documento, String agencia, String numero) {
        return KEY_PREFIX + documento + ":" + agencia + ":" + numero;
    }
}
