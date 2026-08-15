package com.darlanmarangoni.redis.transacao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public Mono<ResponseEntity<Transacao>> receber(@RequestBody Transacao transacao) {
        Transacao comId = new Transacao(
                UUID.randomUUID().toString(),
                transacao.clienteOrigem(),
                transacao.clienteDestino(),
                transacao.contaOrigem(),
                transacao.contaDestino(),
                transacao.valor());

        return transacaoService.salvar(comId
                )
                .map(salva -> ResponseEntity.status(HttpStatus.CREATED).body(salva));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Transacao>> buscar(@PathVariable String id) {
        return transacaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
