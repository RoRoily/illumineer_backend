package com.buaa01.illumineer_backend.service.paper;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.PaperAdo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface PaperService {

    /**
     * 推荐
     * @param num 数据条数
     */
    CustomResponse getRecommend(Integer num);

    /**
     * 根据 pid 返回引用量
     *
     * @param pid 文章 id
     * @return 引用量
     */
    CustomResponse getRefTimes(Long pid);

    /**
     * 根据 pid 增加引用量
     *
     * @param pid 文章 id
     */
    CustomResponse addRefTimes(Long pid);

    /**
     * 根据 pid 增加收藏量
     *
     * @param pid 文章 id
     */
    CustomResponse addFavTimes(Long pid);

    /**
     * 根据 pid 上传新的文章
     *
     * @param paper   文章
     * @param content 文章内容（文件）
     */
    CustomResponse uploadPaper(Paper paper, MultipartFile content, Integer uid);

    /**
     * 修改文章信息
     *
     * @param
     * @return
     */
    CustomResponse updatePaper(Long pid,
                               String title,
                               String essAbs,
                               List<String> keywords,
                               MultipartFile content,
                               Map<String, Integer> auths,
                               String field,
                               String type,
                               String theme,
                               String publishDate,
                               String derivation,
                               List<Long> refs);

    /**
     * 查找用户收藏夹内所有文献
     *
     * @param fid 收藏夹id
     * @return CustomResponse
     */

    CustomResponse getPaperByFid(Integer fid);

    /**
     * 修改文章的归属情况
     * @param pid
     * @param name
     * @param uid
     * @return
     */
    CustomResponse modifyAuth(Long pid,String name,Integer uid);

}
