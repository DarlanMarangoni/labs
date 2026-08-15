package com.darlanmarangoni.redis.transacao;

import java.math.BigDecimal;

public record Transacao(
        String id,
        Cliente clienteOrigem,
        Cliente clienteDestino,
        Conta contaOrigem,
        Conta contaDestino,
        BigDecimal valor) {
}
