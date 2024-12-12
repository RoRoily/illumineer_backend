package com.buaa01.illumineer_backend.service.client;

import com.buaa01.illumineer_backend.config.FeignConfig;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.PaperAdo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//服务名称和url
//用于接收paper微服务模块传输的对象
//自动将调用路由到' document-service'
@FeignClient(name = "document-service", configuration = FeignConfig.class/*fallback = PaperServiceClientFallback.class*/)
public interface PaperServiceClient {

    //从paperService中寻找提供的服务
    @GetMapping("/paper/{pid}")
    Paper getPaperById(@PathVariable("pid") Integer pid);

    @GetMapping("/paper/{name}")
    List<PaperAdo> getPaperAdoByName(@PathVariable("name") String name);

    /*@GetMapping("/paper/paperStatus/{pid}")
    PaperStatus getPaperStatusById(@PathVariable("pid") Integer pid);*/

    @PostMapping("/paper/updateStatus")
    CustomResponse updatePaperStatus(@RequestParam("pid") Integer pid,
                                     @RequestParam("statusType") String statusType,
                                     @RequestParam("increment") Boolean increment,
                                     @RequestParam("count") Integer count);

    @GetMapping("/paper/subList")
    List<PaperAdo> getPaperAdoByList(List subList);

    @GetMapping("/paper/propider/test/{message}")
    public String getPropiderTest(@PathVariable("message") String message);

    @GetMapping("/paper/propider/sentinel/test/{message}")
    public String propiderSentinelTest(@PathVariable("message") String message);

    @GetMapping("/paper/getByFid/{fid}")
    CustomResponse getPaperByFid(@PathVariable("fid") Integer fid);
}
