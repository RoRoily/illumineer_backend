package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.SearchResultPaper;
import com.buaa01.illumineer_backend.service.paper.PaperEsUploadService;
import com.buaa01.illumineer_backend.utils.FilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PaperEsUploadController {
    @Autowired
    private PaperEsUploadService paperEsUploadService;

    @PostMapping("/upload/Es")
    public CustomResponse uploadES() {
       paperEsUploadService.UploadPaperInEs();
       return new CustomResponse(200,"更新成功",null);
    }
}
