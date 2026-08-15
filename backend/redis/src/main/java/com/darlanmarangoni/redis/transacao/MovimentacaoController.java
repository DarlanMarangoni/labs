package com.darlanmarangoni.redis.transacao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @GetMapping("/{documento}/{agencia}/{numero}")
    public Mono<ResumoMovimentacao> consultar(
            @PathVariable String documento,
            @PathVariable String agencia,
            @PathVariable String numero) {
        return movimentacaoService.resumo(documento, agencia, numero);
    }
}
