package com.buaa01.illumineer_backend.service.client;


import com.bilimili.buaa13.config.FeignConfig;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.Video;
import com.bilimili.buaa13.entity.VideoStatus;
import com.bilimili.buaa13.service.fallback.VideoServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

//服务名称和url
//用于接收video微服务模块传输的对象
//自动将调用路由到'video-service'
@FeignClient(name = "video-service", configuration = FeignConfig.class,fallback = VideoServiceClientFallback.class)
public interface VideoServiceClient {

    //从videoService中寻找提供的服务
    @GetMapping("/video/{vid}")
    Video getVideoById(@PathVariable("vid") Integer vid);

    @GetMapping("/video/videoStatus/{vid}")
    VideoStatus getVideoStatusById(@PathVariable("vid") Integer vid);

    @PostMapping("/video/updateStatus")
    ResponseResult updateVideoStatus(@RequestParam("vid") Integer vid,
                                     @RequestParam("statusType") String statusType,
                                     @RequestParam("increment") Boolean increment,
                                     @RequestParam("count") Integer count);


    @PostMapping("/video/updateGoodAndBad")
    ResponseResult updateGoodAndBad(@RequestParam("vid") Integer vid,
                                    @RequestParam("addGood") Boolean addGood
    );
    @GetMapping("/video/provider/test/{message}")
    public String getProviderTest(@PathVariable("message") String message);

    @GetMapping("/video/provider/sentinel/test/{message}")
    public String providerSentinelTest(@PathVariable("message") String message);
}
