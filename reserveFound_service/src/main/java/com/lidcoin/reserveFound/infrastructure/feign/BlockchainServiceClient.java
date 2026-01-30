package com.lidcoin.reserveFound.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "blockchain-service", url = "${blockchain-service.url:http://localhost:8082}")
public interface BlockchainServiceClient {

    @PostMapping("/api/blockchain/transactions")
    Map<String, Object> recordTransaction(@RequestBody Map<String, Object> transaction);

    @PostMapping("/api/blockchain/validate")
    Map<String, Object> validateTransaction(@RequestBody Map<String, Object> transaction);
}