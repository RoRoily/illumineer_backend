package com.buaa01.illumineer_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Papers;
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
     * 
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
     * 
     * @param condition 筛选条件（选择查找的字段）
     * @param keyword 搜索内容
     * @param size 一页多少条内容
     * @param offset 第几页
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order 0=降序，1=升序
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
     * @param conditions 条件：logic(none=0/and=1/or=2/not=3), condition, keyword
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
     * 
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
     * 
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
     * 
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
     * 
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
            @RequestParam("keywords") String keywords,
            @RequestParam("content") MultipartFile content,
            @RequestParam("field") String field,
            @RequestParam("type") String type,
            @RequestParam("theme") String theme,
            @RequestParam("publishDate") Date publishDate,
            @RequestParam("derivation") String derivation) {
        Papers paper = new Papers(
                0,
                title,
                essAbs,
                Arrays.stream(keywords.split(" ")).toList(),
                null,
                null,
                Arrays.stream(field.split(" ")).toList(),
                type,
                theme,
                publishDate,
                derivation,
                null,
                0);
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
     * 
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
     * 
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
     * 
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
     * 
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
    public CustomResponse updatePaper(@RequestParam("pid") int pid,
            @RequestParam("title") String title,
            @RequestParam("essAbs") String essAbs,
            @RequestParam("keywords") String keywords,
            @RequestParam("content") MultipartFile content,
            @RequestParam("field") String field,
            @RequestParam("type") String type,
            @RequestParam("theme") String theme,
            @RequestParam("publishDate") Date publishDate,
            @RequestParam("derivation") String derivation) {
        try {
            return paperService.updatePaper(pid, title, essAbs, keywords, content, field, type, theme, publishDate,
                    derivation);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("文章更新失败！");
            return customResponse;
        }
    }

}
