package com.darlanmarangoni.redis.transacao;

import java.util.List;

public record Conta(String agencia, String numero, List<String> chavesPix) {
}
