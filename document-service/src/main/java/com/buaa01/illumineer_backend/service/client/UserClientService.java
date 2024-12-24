package com.buaa01.illumineer_backend.service.client;

import com.buaa01.illumineer_backend.config.FeignConfig;
import com.buaa01.illumineer_backend.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserClientService {
    @GetMapping("/user/currentUser")
    User getCurrentUser();
}
