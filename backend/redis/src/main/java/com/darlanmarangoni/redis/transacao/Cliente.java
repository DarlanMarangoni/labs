package com.darlanmarangoni.redis.transacao;

import java.util.List;

public record Cliente(String nome, String documento, List<Conta> contas) {
}
