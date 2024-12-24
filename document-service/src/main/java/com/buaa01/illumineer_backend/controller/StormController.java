package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.StormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

@RestController
public class StormController {
    @Autowired
    private StormService stormService;

    @PostMapping("/admin/updatePaper")
    public CustomResponse updatePaper() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
        CompletableFuture<String> future = stormService.getStorm();
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                // 异常处理
                System.out.println("Exception: " + exception.getMessage());
                // 这里可以返回一个失败的响应或者做其他处理
            } else {
                // 异常完成后的处理
                System.out.println("Task completed successfully with result: " + result);
                // 如果你需要返回结果，可以通过其他方式返回
            }
        });
        // 这里会立即返回响应，而不等待异步任务完成
        return new CustomResponse(200, "OK", "Processing...");
    }
}
