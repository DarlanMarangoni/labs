package com.darlanmarangoni.redis.transacao;

import java.math.BigDecimal;

public record JanelaMovimentacao(BigDecimal enviado, BigDecimal recebido, BigDecimal total) {
}
