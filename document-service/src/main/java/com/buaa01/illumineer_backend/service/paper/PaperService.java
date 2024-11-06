package com.buaa01.illumineer_backend.service.paper;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public interface PaperService {
    /**
     * 根据pid查询文献信息
     * @param pid 文献ID
     * @return Paper 文献实体类
     */
    CustomResponse getPaperByPid(Integer pid);

    /**
     * 一框式检索接口：搜索文献（分页、排序）
     * @param keyword 搜索内容
     * @param offset 第几页
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @return 文献信息
     */
    CustomResponse searchPapers(String condition, String keyword, Integer size, Integer offset, Integer sortType);

    /**
     * 根据 pid 返回引用量
     * @param pid 文章 id
     * @return 引用量
     */
    CustomResponse getRefTimes(int pid);

    /**
     * 根据 pid 增加引用量
     * @param pid 文章 id
     */
    CustomResponse addRefTimes(int pid);

    /**
     * 根据 pid 增加收藏量
     * @param pid 文章 id
     */
    CustomResponse addFavTimes(int pid);

    /**
     * 根据 pid 上传新的文章
     * @param paper 文章
     * @param content 文章内容（文件）
     */
    CustomResponse uploadPaper(Paper paper, MultipartFile content);

    /**
     * 更新作者（已认证）
     * @param pid
     * @param aid
     * @return
     */
    CustomResponse updateAuth(int pid, int aid);

    /**
     * 更新作者（已认证）
     * @param pid
     * @param author
     * @return
     */
    CustomResponse updateAuth(int pid, String author);

    /**
     * 删除文章
     * @param pid
     * @return
     */
    CustomResponse deletePaper(int pid);

    /**
     * 修改文章信息
     * @param
     * @return
     */
    CustomResponse updatePaper(int pid,
                               String title,
                               String essAbs,
                               String keywords,
                               MultipartFile content,
                               String field,
                               String type,
                               String theme,
                               Date publishDate,
                               String derivation);
}
