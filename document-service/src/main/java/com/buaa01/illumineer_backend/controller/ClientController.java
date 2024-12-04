package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.PaperAdo;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperAdoptionService;
import com.buaa01.illumineer_backend.service.paper.PaperService;
import com.buaa01.illumineer_backend.service.paper.PaperStatsService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 来自document-service模块，向user-service提供被需求服务
 *
 *
 **/
@Service
@RestController
@RequestMapping("/document")
public class ClientController {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperAdoptionService paperAdoptionService;

    @Autowired
    private PaperStatsService paperStatsService;

    /***
     * 根据文章列表返回对应文章列表
     * 
     * @param pids 文章id列表
     **/
    @PostMapping("/paper/subList")
    public CustomResponse getPaperAdoptionsByList(List<Long> pids) {
        CustomResponse customResponse = new CustomResponse();
        try {
            customResponse.setData(paperAdoptionService.getPaperAdoptionsByList(pids));
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取认领条目列表！");
        }
        return customResponse;
    }

    @GetMapping("/paper/{pid}")
    Paper getPaperById(@PathVariable("pid") Integer pid) {
        return paperMapper.getPaperByPid(pid);
    }

    @GetMapping("/paper/{name}")
    List<PaperAdo> getPaperAdoByName(@PathVariable("name") String name) {
        return (List<PaperAdo>) paperAdoptionService.getPaperAdoptionsByName(name).getData();
    }

    @PostMapping("/paper/updateStatus")
    CustomResponse updatePaperStatus(@RequestParam("pid") Integer pid,
            @RequestParam("statusType") String statusType,
            @RequestParam("increment") Boolean increment,
            @RequestParam("count") Integer count) {
        return paperStatsService.updateStats(pid, statusType, increment, count); // FIXME: 参数不匹配
    }

    @GetMapping("/paper/subList")
    List<PaperAdo> getPaperAdoByList(List<Long> subList) {
        return paperAdoptionService.getPaperAdoptionsByList(subList);
    }

    @GetMapping("/paper/propider/test/{message}")
    public String getPropiderTest(@PathVariable("message") String message) { // FIXME: 待完成PaperServiceClient.java中的相关问题
        return 
    }

    @GetMapping("/paper/propider/sentinel/test/{message}")
    public String propiderSentinelTest(@PathVariable("message") String message) { // FIXME:
                                                                                  // 待完成PaperServiceClient.java中的相关问题

    }

    // @PostMapping("/document/adoption")
    // CustomResponse updatePaperAdoptionStatus(@RequestParam("name") String name){
    // CustomResponse customResponse = new CustomResponse();
    // paperService.updatePaperAdoptionStatus(name);
    // return customResponse;
    // }

    // // 通过视频ID获取视频详情
    // @GetMapping("/{vid}")
    // @SentinelResource(value = "getVideoById",blockHandler =
    // "getVideoByIdHandler")
    // public ResponseResult getVideoById(@PathVariable("vid") Integer vid) {
    // Video video = videoMapper.selectById(vid);
    // if (video != null) {
    // return new ResponseResult(200, "OK", video);
    // } else {
    // return new ResponseResult(404, "Video not found", null);
    // }
    // }
    // public ResponseResult getVideoByIdHandler(@PathVariable("vid") Integer vid,
    // BlockException exception) {
    // return new ResponseResult(404, "Video not found fallback", null);
    // }
    //
    // // 通过视频ID获取视频状态
    // @GetMapping("/videoStatus/{vid}")
    // @SentinelResource(value = "getVideoStatusById",blockHandler =
    // "getVideoStatusByIdHandler")
    // public VideoStatus getVideoStatusById(@PathVariable("vid") Integer vid) {
    // return videoStatusService.getStatusByVideoId(vid);
    // }
    // public ResponseResult getVideoStatusByIdHandler(@PathVariable("vid") Integer
    // vid, BlockException exception) {
    // return new ResponseResult(404, "VideoStatus not found fallback", null);
    // }
    //
    // // 更新视频状态
    // @PostMapping("/updateStatus")
    // @SentinelResource(value = "updateVideoStatus",blockHandler =
    // "updateVideoStatusHandler")
    // public ResponseResult updateVideoStatus(@RequestParam("vid") Integer vid,
    // @RequestParam("statusType") String statusType,
    // @RequestParam("increment") Boolean increment,
    // @RequestParam("count") Integer count) {
    //
    // try {
    // videoStatusService.updateVideoStatus(vid, statusType, increment, count);
    // return new ResponseResult(200, "update video status success", null);
    // } catch (Exception e) {
    // return new ResponseResult(500, "update video status failed", null);
    // }
    // }
    //
    // public ResponseResult updateVideoStatusHandler(@PathVariable("vid") Integer
    // vid,
    // @RequestParam("statusType") String statusType,
    // @RequestParam("increment") Boolean increment,
    // @RequestParam("count") Integer count,
    // BlockException exception) {
    // return new ResponseResult(404, "update VideoStatus not found fallback",
    // null);
    // }
    //
    // // 更新视频的点赞或差评数
    // @PostMapping("/updateGoodAndBad")
    // @SentinelResource(value = "updateGoodAndBad",blockHandler =
    // "updateGoodAndBadHandler")
    // public ResponseResult updateGoodAndBad(@RequestParam("vid") Integer vid,
    // @RequestParam("addGood") Boolean addGood) {
    // try {
    // videoStatusService.updateGoodAndBad(vid, addGood);
    // return new ResponseResult(200, "update video status success", null);
    // } catch (Exception e) {
    // return new ResponseResult(500, "update video status failed", null);
    // }
    // }
    // public ResponseResult updateGoodAndBadHandler(@RequestParam("vid") Integer
    // vid,
    // @RequestParam("addGood") Boolean addGood,
    // BlockException exception) {
    // return new ResponseResult(404, "update GoodBad not found fallback", null);
    // }
    //
    // @GetMapping("/provider/sentinel/test/{message}")
    // @SentinelResource(value = "providerSentinelTest", blockHandler =
    // "handlerBlockHandler")
    // public String providerSentinelTest(@PathVariable("message") String message) {
    // return "sentinel测试：" + message;
    // }
    //
    // public String handlerBlockHandler(@PathVariable("message") String message,
    // BlockException exception) {
    // return "providerSentinelTest服务不可用，" + "触发sentinel流控配置规则"+"\t"+"o(╥﹏╥)o";
    // }
}