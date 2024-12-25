package com.buaa01.illumineer_backend.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.PaperAdo;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.CategoryService;
import com.buaa01.illumineer_backend.service.paper.PaperAdoptionService;
import com.buaa01.illumineer_backend.service.paper.PaperSearchService;
import com.buaa01.illumineer_backend.service.paper.PaperService;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

/**
 * 来自document-service模块，向user-service提供被需求服务
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
    private PaperSearchService paperSearchService;

    @Autowired
    private PaperAdoptionService paperAdoptionService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/paper")
    @SentinelResource(value = "getPaperById",blockHandler = "getPaperByIdHandler")
    public Map<String, Object> getPaperById(@RequestParam("pid") Long pid) {
        Map<String, Object> paper = paperMapper.getPaperByPid(pid);
        return paper;
    }
    public List<PaperAdo> getPaperByIdHandler(@RequestParam("pids") String pids, @RequestParam("name")String name) {
        return null;
    }

    /***
     * 根据文章列表返回对应文章列表
     *
     * @param pids 文章id列表
     **/
    @GetMapping("/ado/subList")
    //@SentinelResource(value = "getPaperAdoptionsByList",blockHandler = "getPaperAdoptionsByListHandler")
    public List<PaperAdo> getPaperAdoptionsByList(@RequestParam("pids") String pids) {
        String[] pidss = pids.split(":");
        List<Long> subList = Arrays.stream(pidss[1].split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        String name = pidss[0];
        return paperAdoptionService.getPaperAdoptionsByList(subList, name);
    }

    /***
     * 根据文章列表返回对应文章列表
     *
     * @param pids 文章id列表
     **/
    @GetMapping("/ado/subList2")
    @SentinelResource(value = "getPaperAdoptionsByList",blockHandler = "getPaperAdoptionsByListHandler")
    public List<PaperAdo> getPaperAdoptionsByList2(@RequestParam("pids") String pids, @RequestParam("name")String name) {
        List<Long> subList = Arrays.stream(pids.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return paperAdoptionService.getPaperAdoptionsByList(subList, name);
    }
    public List<PaperAdo> getPaperAdoptionsByListHandler(@RequestParam("pids") String pids, @RequestParam("name")String name) {
        return new ArrayList<>();
    }


    /***
     * 根据category返回该category的认领条目列表
     * @param category
     * @param total 总数
     * **/
    @GetMapping("/ado/category")
    @SentinelResource(value = "getPaperAdoptionsByName",blockHandler = "getPaperAdoptionsByNameHandler")
    public CustomResponse getPaperAdoptionsByName(@RequestParam("category") Category category, @RequestParam("total") Integer total) {
        CustomResponse customResponse = new CustomResponse();
        try {
            customResponse.setData(paperAdoptionService.getPaperAdoptionsByCategory(category, total));
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取认领条目列表！");
        }
        return customResponse;
    }
    public CustomResponse getPaperAdoptionsByNameHandler(@RequestParam("category") Category category, @RequestParam("total") Integer total) {
       return new CustomResponse(200,"熔断，请稍后再试",null);
    }


    /**
     * 查找用户收藏夹内所有文献
     *
     * @param fid 收藏夹id
     * @return CustomResponse
     */

    @GetMapping("/paper/getByFid")
    @SentinelResource(value = "getPaperByFid",blockHandler = "getPaperByFidHandler")
    public CustomResponse getPaperByFid(@RequestParam("fid") Integer fid) {
        return paperService.getPaperByFid(fid);
    }
    public CustomResponse getPaperByFidHandler(@RequestParam("fid") Integer fid) {
        return new CustomResponse(200,"熔断，请稍后再试",null);
    }

    @GetMapping("/paper/getCategory")
    @SentinelResource(value = "getCategory",blockHandler = "getCategoryHandler")
    List<String> getCategory(@RequestParam List<String> ids) throws JsonProcessingException {
        List<String> result = new ArrayList<>();
        for (String id : ids)
            result.add(categoryService.getCategoryByID(id).toJsonString());
        return result;
    }
    List<String> getCategoryHandler(@RequestParam List<String> ids) throws JsonProcessingException {
        return new ArrayList<>();
    }


    /**
     * 修改文章的所有者情况
     *
     * @param Pid
     * @param name
     * @param uid
     * @return
     */
    @PostMapping("/paper/modiftAuth")
    @SentinelResource(value = "modifyAutb",blockHandler = "modifyAuthHandler")
    CustomResponse modifyAuth(@RequestParam("pid")Long Pid,
                              @RequestParam("name")String name,
                              @RequestParam("uid")Integer uid
    ){
        System.out.println("ok");
        return paperService.modifyAuth(Pid,name,uid);
    }
    CustomResponse modifyAuthHandler(@RequestParam("pid")Long Pid,
                              @RequestParam("name")String name,
                              @RequestParam("uid")Integer uid
    ){
       return new CustomResponse(200,"熔断,请稍等",null);
    }

    /**
     * 通过姓名获取id
     * @param name
     * @param pid
     * @return
     */
    @GetMapping("/paper/getAuthUid")

    Integer getAuthId(@RequestParam("name")String name,@RequestParam("pid")Long pid)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> paper = paperMapper.getPaperByPid(pid);
        Integer uid=null;

        try {
            // auths 的转换
            Map<String, Integer> auths = objectMapper.readValue(paper.get("auths").toString(), new TypeReference<Map<String, Integer>>() {});
            paper.put("auths", auths);
            uid = auths.get(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uid;
    }
    @GetMapping("/paper/{name}")
    List<PaperAdo> getPaperAdoByName(@PathVariable("name") String name){
        CustomResponse customResponse = paperAdoptionService.getPaperAdoptionsByName(name);
        Map<String,Object> map = (Map<String, Object>) customResponse.getData();
        return (List<PaperAdo>) map.get("result");
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