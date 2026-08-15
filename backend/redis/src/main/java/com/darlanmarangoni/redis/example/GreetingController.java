package com.darlanmarangoni.redis.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/greetings")
public class GreetingController {

    private final GreetingService greetingService;

    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @PostMapping
    public Mono<ResponseEntity<Greeting>> create(@RequestBody Greeting greeting) {
        return greetingService.save(greeting)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(greeting));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Greeting>> get(@PathVariable String id) {
        return greetingService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return greetingService.deleteById(id)
                .map(deleted -> ResponseEntity.noContent().<Void>build());
    }
}
