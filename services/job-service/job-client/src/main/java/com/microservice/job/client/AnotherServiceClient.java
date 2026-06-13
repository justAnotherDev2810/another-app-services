package com.microservice.job.client;

import com.microservice.job.api.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
 
/**
 * Feign client interface for calling another-service.
 *
 * Any service that needs to call another-service:
 * 1. Imports job-client as a dependency
 * 2. Adds @EnableFeignClients(basePackages = "com.microservice.job.client")
 * 3. Autowires this interface — Spring generates the HTTP implementation
 *
 * url pulled from yml — never hardcoded.
 * In the consuming service's application.yml:
 *   clients:
 *     another-service:
 *       base-url: http://localhost:8091
 */
@FeignClient(
        name = "another-service-client",
        url = "${clients.another-service.url}"
)
public interface AnotherServiceClient {
 
    @PostMapping("/api/user")
    UserDto createUser(@RequestBody UserDto userDto);
}