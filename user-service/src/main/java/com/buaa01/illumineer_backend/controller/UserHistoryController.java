package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.History;
import com.buaa01.illumineer_backend.service.history.HistoryService;
import com.buaa01.illumineer_backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserHistoryController {

    @Autowired
    private HistoryService historyService;
    @Autowired
    private CurrentUser currentUser;

    /**
     * 在当前登录用户的历史记录中新增一个条目
     * @param pid 文献对应的pid
     * @return customeResponce实体类
     */
    @PostMapping("/user/history/update")
    public CustomResponse updateOne(@RequestParam("pid")Integer pid){
        return historyService.insertInHistory(pid);
    }

    /**
     * 分页返回历史记录中的条目
     * @param quantity,index 每次需要返回的条目数量  偏移量，用于定位需要返回的页面
     * @return CunstomResponce实体类
     */
    @GetMapping("/user/history/getAPage")
    public CustomResponse getAPage(@RequestParam("quantity")Integer quantity,@RequestParam("index")Integer index){
        return historyService.getHistoryByPage(currentUser.getUserUid(),quantity,index);
    }
}
