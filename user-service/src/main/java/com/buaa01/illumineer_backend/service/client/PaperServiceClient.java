package com.buaa01.illumineer_backend.service.client;

import com.buaa01.illumineer_backend.config.FeignConfig;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.PaperAdo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//服务名称和url
//用于接收paper微服务模块传输的对象
//自动将调用路由到' document-service'
//@FeignClient(name = "document-service", configuration = FeignConfig.class/*fallback = PaperServiceClientFallback.class*/)
@FeignClient(name = "document-service")
public interface PaperServiceClient {

    //从paperService中寻找提供的服务
    @GetMapping("/document/paper/{pid}")
    Paper getPaperById(@PathVariable("pid") Integer pid);


    @GetMapping("/document/paper/getAuthUid")
    Integer getAuthId(@RequestParam("name")String name,@RequestParam("pid")Long pid);

    @GetMapping("/document/paper/{name}")
    List<PaperAdo> getPaperAdoByName(@PathVariable("name") String name);

    /*@GetMapping("/paper/paperStatus/{pid}")
    PaperStatus getPaperStatusById(@PathVariable("pid") Integer pid);*/

    @PostMapping("/document/paper/updateStatus")
    CustomResponse updatePaperStatus(@RequestParam("pid") Integer pid,
                                     @RequestParam("statusType") String statusType,
                                     @RequestParam("increment") Boolean increment,
                                     @RequestParam("count") Integer count);

    @GetMapping("/document/ado/subList")
    List<PaperAdo> getPaperAdoByList(@RequestParam("pids") String subList,
                                     @RequestParam("name") String name);

    @GetMapping("/document/paper/propider/test/{message}")
    public String getPropiderTest(@PathVariable("message") String message);

    @GetMapping("/document/paper/propider/sentinel/test/{message}")
    public String propiderSentinelTest(@PathVariable("message") String message);

    @GetMapping("/document/paper/getByFid")
    CustomResponse getPaperByFid(@RequestParam("fid") Integer fid);

    //FIXME:在函数头添加了document
    @PostMapping("/document/paper/modiftAuth")
    CustomResponse modifyAuth(@RequestParam("pid")Long Pid,
                              @RequestParam("name")String name,
                              @RequestParam("uid")Integer uid
    );

    @GetMapping("/document/paper/getCategory")
    List<String> getCategory(@RequestParam List<String> ids);
}

