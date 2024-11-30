package com.buaa01.illumineer_backend.service.client;

import com.buaa01.illumineer_backend.entity.User;
import org.springframework.web.bind.annotation.GetMapping;

public interface UserClientService {
    @GetMapping("/user/currentUser")
    User getCurrentUser();
}
