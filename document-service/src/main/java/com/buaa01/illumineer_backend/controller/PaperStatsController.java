package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.paper.PaperStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaperStatsController {

    @Autowired
    private PaperStatsService paperStatsService;

    /**
    * @description: 更新文章状态（0 正常 1 已删除 2 待审核）
     * 下架文献：stats = 1
     * 刚上传文献 / 文献被投诉：stats = 2
     * 文献审核完毕：stats = 0
    * @param: [pid, stats 要更新成什么状态]
    * @return: 是否成功更新文章状态
    **/
    @PostMapping("/updateStats")
    public CustomResponse updateStats(@RequestParam("pid") Long pid, @RequestParam("stats") Integer stats) {
        try {
            return paperStatsService.updateStats(pid, stats);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法更新文章状态");
            return customResponse;
        }
    }
}
