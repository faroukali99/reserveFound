package com.lidcoin.reserveFound.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    Map<String, Object> getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/username/{username}")
    Map<String, Object> getUserByUsername(@PathVariable("username") String username);

    @GetMapping("/api/users/email/{email}")
    Map<String, Object> getUserByEmail(@PathVariable("email") String email);
}