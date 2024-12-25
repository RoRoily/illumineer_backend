package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.LoadMetrics;
import com.buaa01.illumineer_backend.service.LoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoadController {

    @Autowired
    private LoadService loadService;

    /**
     * 获取系统负载信息
     *
     * @return 负载指标
     */
    @GetMapping("/system/load")
    public LoadMetrics getSystemLoad() {
        return loadService.getSystemLoad();
    }
}
