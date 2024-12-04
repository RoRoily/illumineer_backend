package com.buaa01.illumineer_backend.service.client;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public interface UserClientService {
    @GetMapping("/user/currentUser")
    User getCurrentUser();

    @GetMapping("/user/{uid}")
    @SentinelResource(value = "getUserById", blockHandler = "getUserByIdHandler")
    public User getUserById(@PathVariable("uid") Integer uid);

    @PostMapping("/user/currentUser/getId")
    @SentinelResource(value = "getCurrentUserId", blockHandler = "getCurrentUserIdHandler")
    public Integer getCurrentUserId();

    @PostMapping("/user/currentUser/isAdmin")
    @SentinelResource(value = "currentIsAdmin", blockHandler = "currentIsAdminHandler")
    public Boolean currentIsAdmin();

    @PostMapping("/user/set/authPaper")
    @SentinelResource(value = "setAuthPaper", blockHandler = "setAuthPaperHandler")
    public CustomResponse setAuthPaper(@RequestParam("add") Integer add,
            @RequestParam("pids") String pids);

    @GetMapping("/user/getUserByName/{account}")
    User getUserByName(@PathVariable("account") String account);
}
