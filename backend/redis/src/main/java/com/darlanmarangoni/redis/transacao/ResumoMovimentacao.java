package com.darlanmarangoni.redis.transacao;

public record ResumoMovimentacao(
        JanelaMovimentacao ultimos2Minutos,
        JanelaMovimentacao ultimos5Minutos,
        JanelaMovimentacao ultimaHora) {
}
