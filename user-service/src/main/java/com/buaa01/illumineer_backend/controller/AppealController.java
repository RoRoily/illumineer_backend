package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.user.AppealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.relational.core.sql.In;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AppealController {

    @Autowired
    private AppealService appealService;

    /**
     * 创建申诉条目
     * @param appellantUid
     * @param sameName
     * @param conflictPaperPid
     * @return
     */
    @PostMapping("/appeal/create")
    public CustomResponse createAppeal(@RequestParam("appellantUid")Integer appellantUid,
                                       @RequestParam("sameName")String sameName,
                                       @RequestParam("conflictPaperPid")Integer conflictPaperPid){

            return appealService.createAppealEntry(appellantUid,sameName,conflictPaperPid);
    }
    
    /**
     * 处理申诉条目
     * @param appealEntryId
     * @param acceptAppeal
     * @return
     */
    @PostMapping("/appeal/judge")
    public CustomResponse judgeAppeal(
            @RequestParam("appealEntryId")Integer appealEntryId,
            @RequestParam("acceptAppeal")boolean acceptAppeal){

        return appealService.judgeAppeal(appealEntryId,acceptAppeal);
    }

    /**
     * 分页显示申诉条目
     * @param quantity
     * @param index
     * @param handled
     * @return
     */
    @GetMapping("/appeal/getpage")
    public CustomResponse displayAppeal(
            @RequestParam("quantity")Integer quantity,
            @RequestParam("index")Integer index,
            @RequestParam("handled")Boolean handled
    ){
        return appealService.displayAppealEntry(quantity,index,handled);
    }
}
