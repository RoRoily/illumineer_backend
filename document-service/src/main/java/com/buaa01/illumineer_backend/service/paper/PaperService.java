package com.buaa01.illumineer_backend.service.paper;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Papers;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PaperService {

    /**
     * 根据 pid 返回引用量
     * 
     * @param pid 文章 id
     * @return 引用量
     */
    CustomResponse getRefTimes(int pid);

    /**
     * 根据 pid 增加引用量
     * 
     * @param pid 文章 id
     */
    CustomResponse addRefTimes(int pid);

    /**
     * 根据 pid 增加收藏量
     * 
     * @param pid 文章 id
     */
    CustomResponse addFavTimes(int pid);

    /**
     * 根据 pid 上传新的文章
     * 
     * @param paper   文章
     * @param content 文章内容（文件）
     */
    CustomResponse uploadPaper(Papers paper, MultipartFile content);

    /**
     * 更新文章信息
     * 
     * @param
     * @return
     */
    CustomResponse updatePaper(int pid,
            String title,
            String essAbs,
            List<String> keywords,
            MultipartFile content,
            Map<String, Integer> auths,
            List<Category> field,
            String type,
            String theme,
            LocalDate publishDate,
            String derivation,
            List<Integer> refs);
}
