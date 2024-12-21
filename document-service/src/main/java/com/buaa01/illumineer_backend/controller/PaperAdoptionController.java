package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.PaperAdo;
import com.buaa01.illumineer_backend.service.paper.PaperAdoptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaperAdoptionController {

    @Autowired
    private PaperAdoptionService paperAdoptionService;

    /***
     * 返回该用户已认领的文献
     * @param name 姓名
     * **/
    @GetMapping("/belong/name")
    public CustomResponse getPaperBelongedByName(@RequestParam("name") String name){
        try {
            return paperAdoptionService.getPaperBelongedByName(name);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取认领的文献！");
            return customResponse;
        }
    }

    /***
     * 根据作者姓名返回包含该姓名的认领条目列表
     * @param name 姓名
     * **/
    @GetMapping("/ado/name")
    public CustomResponse getPaperAdoptionsByName(@RequestParam("name") String name){
        try {
            return paperAdoptionService.getPaperAdoptionsByName(name);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取认领条目列表！");
            return customResponse;
        }
    }
}
