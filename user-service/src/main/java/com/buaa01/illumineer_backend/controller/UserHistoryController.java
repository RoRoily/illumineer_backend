package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.History;
import com.buaa01.illumineer_backend.service.history.HistoryService;
import com.buaa01.illumineer_backend.service.utils.CurrentUser;
import com.buaa01.illumineer_backend.tool.JsonWebTokenTool;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    @PostMapping("/history/update")
    public CustomResponse updateOne(@RequestParam("pid")Long pid, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if (token.isEmpty()) {
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("缺少token");
        }
        token = token.substring(7);
        String userId = JsonWebTokenTool.getSubjectFromToken(token);
        return historyService.insertInHistory(Integer.parseInt(userId), pid);
    }

    /**
     * 删除历史记录
     * @param pids 文献对应的pid
     * @return customeResponce实体类
     */
    @PostMapping("/history/delete")
    public CustomResponse delete(@RequestParam("pids") List<Long> pids, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if (token.isEmpty()) {
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("缺少token");
        }
        token = token.substring(7);
        String userId = JsonWebTokenTool.getSubjectFromToken(token);
        return historyService.deleteInHistory(Integer.parseInt(userId), pids);
    }

    /**
     * 分页返回历史记录中的条目
     * @return CunstomResponce实体类
     */
    @GetMapping("/history/getAPage")
    public CustomResponse getAPage(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if (token.isEmpty()) {
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("缺少token");
        }
        token = token.substring(7);
        String userId = JsonWebTokenTool.getSubjectFromToken(token);
        return historyService.getHistoryByPage(Integer.parseInt(userId));
//        return historyService.getHistoryByPage(currentUser.getUserUid(),quantity,index);
    }
}
