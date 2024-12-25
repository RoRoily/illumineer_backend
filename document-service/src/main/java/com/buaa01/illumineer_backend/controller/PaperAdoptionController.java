package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.PaperAdo;
import com.buaa01.illumineer_backend.service.paper.PaperAdoptionService;
import com.buaa01.illumineer_backend.service.paper.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class PaperAdoptionController {

    @Autowired
    private PaperAdoptionService paperAdoptionService;
    @Autowired
    private PaperService paperService;

    /***
     * 返回该用户已认领的文献
     * @param name 姓名
     * **/
    @GetMapping("/belong/name")
    public CustomResponse getPaperBelongedByName(@RequestParam("name") String name) {
        try {
            return paperAdoptionService.getPaperBelongedByName(name);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取认领的文献！");
            return customResponse;
        }
    }

    /***
     * 根据作者姓名返回包含该姓名的认领条目列表
     * @param name 姓名
     * **/
    @GetMapping("/ado/name")
    public CustomResponse getPaperAdoptionsByName(@RequestParam("name") String name) {
        try {
            return paperAdoptionService.getPaperAdoptionsByName(name);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取认领条目列表！");
            return customResponse;
        }
    }

    /***
     * 返回该用户G指数和H指数
     * @param name 姓名
     * **/
    @GetMapping("/user/exponent")
    public CustomResponse getUserPaperExponent(@RequestParam("name") String name) {
        try {
            Map<String, Object> result = (Map<String, Object>) paperAdoptionService.getPaperBelongedByName(name).getData();
            List<PaperAdo> paperAdos = (List<PaperAdo>) result.get("result");
            List<Integer> ref_times = new ArrayList<>();
            for (PaperAdo paperAdo : paperAdos) {
                Long pid = paperAdo.getPid();
                Map<String, Integer> map = (Map<String, Integer>) paperService.getRefTimes(pid).getData();
                ref_times.add(map.get("ref_times"));
            }
            // 计算 H 指数
            int hIndex = calculateHIndex(ref_times);
            // 计算 G 指数
            int gIndex = calculateGIndex(ref_times);
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(200);
            customResponse.setMessage("计算成功！");
            customResponse.setData(Map.of("h_index", hIndex, "g_index", gIndex));
            return customResponse;
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法计算相关指数！");
            return customResponse;
        }
    }

    // 计算 H 指数
    public static int calculateHIndex(List<Integer> refTimes) {
        // 按引用次数从大到小排序
        refTimes.sort(Collections.reverseOrder());
        int hIndex = 0;
        for (int i = 0; i < refTimes.size(); i++) {
            if (refTimes.get(i) >= i + 1) {
                hIndex = i + 1;
            } else {
                break;
            }
        }
        return hIndex;
    }

    // 计算 G 指数
    public static int calculateGIndex(List<Integer> refTimes) {
        // 按引用次数从大到小排序
        refTimes.sort(Collections.reverseOrder());
        int gIndex = 0;
        int totalCitations = 0;
        for (int i = 0; i < refTimes.size(); i++) {
            totalCitations += refTimes.get(i);
            if (totalCitations >= (i + 1) * (i + 1)) {
                gIndex = i + 1;
            } else {
                break;
            }
        }
        return gIndex;
    }
}
