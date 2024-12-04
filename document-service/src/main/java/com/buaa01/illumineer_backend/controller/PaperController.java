package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.service.paper.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
public class PaperController {

    @Autowired
    private PaperService paperService;

    /**
     * 根据 pid 返回引用量
     * @param pid 文章 id
     * @return 引用量
     */
    @GetMapping("/get/refTimes")
    public CustomResponse getRefTimes(@RequestParam("pid") int pid) {
        try {
            return paperService.getRefTimes(pid);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取该pid的引用量！");
            return customResponse;
        }
    }

    /**
     * 根据 pid 增加引用量
     * @param pid 文章 id
     * @return
     */
    @PostMapping("/add/refTimes")
    public CustomResponse addRefTimes(@RequestParam("pid") int pid) {
        try {
            return paperService.addRefTimes(pid);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("增加引用量失败！");
            return customResponse;
        }
    }

    /**
     * 根据 pid 增加收藏量
     * @param pid 文章 id
     * @return
     */
    @PostMapping("/add/favTimes")
    public CustomResponse addFavTimes(@RequestParam("pid") int pid) {
        try {
            return paperService.addFavTimes(pid);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("增加收藏量失败！");
            return customResponse;
        }
    }

    /**
     * 上传新的文章
     * @param title
     * @param essAbs
     * @param keywords
     * @param content
     * @param field
     * @param type
     * @param theme
     * @param publishDate
     * @param derivation
     * @return
     */
    @PostMapping("/upload")
    public CustomResponse uploadPaper(@RequestParam("title") String title,
                                      @RequestParam("essAbs") String essAbs,
                                      @RequestParam("keywords") List<String> keywords,
                                      @RequestParam("content") MultipartFile content,
                                      @RequestParam("auths") List<String> auths,
                                      @RequestParam("field") String field,
                                      @RequestParam("type") String type,
                                      @RequestParam("theme") String theme,
                                      @RequestParam("publishDate") Date publishDate,
                                      @RequestParam("derivation") String derivation,
                                      @RequestParam("refs") List<Long> refs,
                                      @RequestParam("category_id") Integer categoryId) {
        // auths
        Map<String, Integer> authsMap = new HashMap<>();
        for (String auth: auths) {
            authsMap.put(auth, -1);
        }
        Paper paper = new Paper(null, title, theme, essAbs, keywords, authsMap, derivation, type, publishDate, field, 0, 0, refs, null, 0, categoryId);
        try {
            return paperService.uploadPaper(paper, content);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("文章上传失败！");
            return customResponse;
        }
    }

    /**
     * 修改文章信息
     * @param pid
     * @param essAbs
     * @param keywords
     * @param content
     * @param field
     * @param type
     * @param theme
     * @param publishDate
     * @param derivation
     * @return
     */
    @PostMapping("/update")
    public CustomResponse updatePaper(@RequestParam("pid") int pid,@RequestParam("title") String title,
                                      @RequestParam("essAbs") String essAbs,
                                      @RequestParam("keywords") List<String> keywords,
                                      @RequestParam("content") MultipartFile content,
                                      @RequestParam("auths") Map<String, Integer> auths,
                                      @RequestParam("field") List<String> field,
                                      @RequestParam("type") String type,
                                      @RequestParam("theme") String theme,
                                      @RequestParam("publishDate") Date publishDate,
                                      @RequestParam("derivation") String derivation,
                                      @RequestParam("refs") List<Integer> refs) {
        try {
            return paperService.updatePaper(pid, title, essAbs, keywords, content, auths, field, type, theme, publishDate, derivation, refs);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("文章更新失败！");
            return customResponse;
        }
    }

}
