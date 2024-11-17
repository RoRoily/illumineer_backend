package com.buaa01.illumineer_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
public class PaperController {

    @Autowired
    private PaperService paperService;

    /**
     * 根据pid获取文献信息
     * @param pid 文献ID
     * @return 文献信息
     */
    @GetMapping("/get")
    public CustomResponse getPaperByPid(@RequestParam("pid") Integer pid) {
        try {
            return paperService.getPaperByPid(pid);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法根据pid获取文献信息！");
            return customResponse;
        }
    }

    /**
     * 一框式检索接口：搜索文献（分页、排序）
     * @param condition 筛选条件（选择查找的字段）
     * @param keyword 搜索内容
     * @param size 一页多少条内容
     * @param offset 第几页
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order 0=降序，1=升序
     * @return 文献信息
     */
    @GetMapping("/search")
    public CustomResponse searchPapers(@RequestParam("condition") String condition,
                                       @RequestParam("keyword") String keyword,
                                       @RequestParam("size") Integer size,
                                       @RequestParam("offset") Integer offset,
                                       @RequestParam("type") Integer sortType,
                                       @RequestParam("order") Integer order) {
        try {
            return paperService.searchPapers(condition, keyword, size, offset, sortType, order);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取文献信息！");
            return customResponse;
        }
    }

    /**
     * 高级检索
     * @param conditions 条件：logic(none=0/and=1/or=2/not=3), condition, keyword（传 name 或者 %name%）
     * @param size 一页多少条内容
     * @param offset 第几页
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order 0=降序，1=升序
     */
    @GetMapping("get/advancedSearch")
    public CustomResponse advancedSearchPapers(@RequestParam("conditions") List<Map<String, String>> conditions,
                                               @RequestParam("size") Integer size,
                                               @RequestParam("offset") Integer offset,
                                               @RequestParam("type") Integer sortType,
                                               @RequestParam("order") Integer order) {
        try {
            return paperService.advancedSearchPapers(conditions, size, offset, sortType, order);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取文献信息！");
            return customResponse;
        }
    }


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
                                      @RequestParam("field") List<String> field,
                                      @RequestParam("type") String type,
                                      @RequestParam("theme") String theme,
                                      @RequestParam("publishDate") Date publishDate,
                                      @RequestParam("derivation") String derivation,
                                      @RequestParam("refs") List<Integer> refs) {
        Map<String, Integer> authsMap = new HashMap<>();
        for (String auth: auths) {
            authsMap.put(auth, -1);
        }
        Paper paper = new Paper(null, title, essAbs, keywords, null, authsMap, field, type, theme, publishDate, derivation, 0, 0, refs, 0);
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
     * 更新作者（已认证）
     * @param pid
     * @param aid
     * @return
     */
//    @PostMapping("/updateAuth")
//    public CustomResponse updateAuth(@RequestParam("pid") int pid,
//                                      @RequestParam("aid") int aid) {
//        try {
//            return paperService.updateAuth(pid, aid);
//        } catch (Exception e) {
//            e.printStackTrace();
//            CustomResponse customResponse = new CustomResponse();
//            customResponse.setCode(500);
//            customResponse.setMessage("作者关联失败！");
//            return customResponse;
//        }
//    }

    /**
     * 更新作者（未认证）
     * @param pid
     * @param author
     * @return
     */
//    @PostMapping("/updateAuth")
//    public CustomResponse updateAuth(@RequestParam("pid") int pid,
//                                      @RequestParam("author") String author) {
//        try {
//            return paperService.updateAuth(pid, author);
//        } catch (Exception e) {
//            e.printStackTrace();
//            CustomResponse customResponse = new CustomResponse();
//            customResponse.setCode(500);
//            customResponse.setMessage("作者关联失败！");
//            return customResponse;
//        }
//    }

    /**
     * 删除文章
     * @param pid
     * @return
     */
    @PostMapping("/delete")
    public CustomResponse deletePaper(@RequestParam("pid") int pid) {
        try {
            return paperService.deletePaper(pid);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("文章删除！");
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
