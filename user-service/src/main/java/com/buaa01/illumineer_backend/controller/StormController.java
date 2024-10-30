package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.StormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StormController {
    @Autowired
    private StormService stormService;

    @PostMapping("/admin/updatePaper")
    public CustomResponse updatePaper() {
        String str = stormService.getStorm();
        return new CustomResponse(200, "OK", str);
    }
}
