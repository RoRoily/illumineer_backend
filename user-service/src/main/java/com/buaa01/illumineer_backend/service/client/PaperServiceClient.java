package com.buaa01.illumineer_backend.service.client;


import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

//服务名称和url
//用于接收document微服务模块传输的对象
//自动将调用路由到' document-service'
@FeignClient(name = "document-service", configuration = FeignConfig.class,fallback = PaperServiceClientFallback.class)
public interface PaperServiceClient {

    //从documentService中寻找提供的服务
    /**
     * 查询函数
     * */
    @GetMapping("/document/{pid}")
    Paper getPaperById(@PathVariable("pid") Integer pid);

    @GetMapping("/document/documentStatus/{pid}")
    PaperStatus getPaperStatusById(@PathVariable("pid") Integer pid);

    @PostMapping("/document/updateStatus")
    CustomResponse updatePaperStatus(@RequestParam("pid") Integer pid,
                                     @RequestParam("statusType") String statusType,
                                     @RequestParam("increment") Boolean increment,
                                     @RequestParam("count") Integer count);


    @GetMapping("/document/propider/test/{message}")
    public String getPropiderTest(@PathVariable("message") String message);

    @GetMapping("/document/propider/sentinel/test/{message}")
    public String propiderSentinelTest(@PathVariable("message") String message);
}
