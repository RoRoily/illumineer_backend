package com.buaa01.illumineer_backend.service.impl.paper;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.client.UserClientService;
import com.buaa01.illumineer_backend.service.paper.PaperService;
import com.buaa01.illumineer_backend.tool.OssTool;
import com.buaa01.illumineer_backend.tool.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class PaperServiceImpl implements PaperService {

    @Autowired
    private PaperMapper paperMapper;
    @Autowired
    private RedisTool redisTool;
    @Autowired
    private UserClientService userClientService;
    @Autowired
    private OssTool ossTool;

    /**
     * 根据 pid 返回引用量
     *
     * @param pid 文章 id
     * @return 引用量
     */
    public CustomResponse getRefTimes(Long pid) {
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> paper = paperMapper.getPaperByPid(pid);
        System.out.println(paper);

        Map<String, Integer> result = new HashMap<>();
        result.put("ref_times", Integer.parseInt(paper.get("ref_times").toString()));
        customResponse.setData(result);
        return customResponse;
    }

    /**
     * 根据 pid 增加引用量
     *
     * @param pid 文章 id
     */
    public CustomResponse addRefTimes(Long pid) {
        CustomResponse customResponse = new CustomResponse();
        UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid);
        updateWrapper.setSql("ref_times = ref_times + 1");

        paperMapper.update(null, updateWrapper);

        customResponse.setMessage("增加引用量成功！");
        return customResponse;
    }

    /**
     * 根据 pid 增加收藏量
     *
     * @param pid 文章 id
     */
    public CustomResponse addFavTimes(Long pid) {
        CustomResponse customResponse = new CustomResponse();
        UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid);
        updateWrapper.setSql("fav_time = fav_time + 1");

        paperMapper.update(null, updateWrapper);

        customResponse.setMessage("增加收藏量成功！");
        return customResponse;
    }

    /**
     * 根据 pid 上传新的文章
     *
     * @param paper   文章
     * @param content 文章内容（文件）
     */
    public CustomResponse uploadPaper(Paper paper, MultipartFile content) {
        CustomResponse customResponse = new CustomResponse();

        // 保存文件到 OSS，返回URL
        String contentUrl = null;
        try {
            contentUrl = ossTool.uploadDocument(content);
        } catch (IOException e) {
            e.printStackTrace();
            customResponse.setCode(505);
            customResponse.setMessage("无法存储OSS");
            return customResponse;
        }

        if (contentUrl == null) {
            log.warn("OSS URL 为空，合并操作终止");
            customResponse.setMessage("无法生成文章url！");
            customResponse.setCode(500);
            return customResponse;
        }

        // 将文章信息封装
        paper.setContentUrl(contentUrl);

        // 存入数据库
        paperMapper.insertPaper(paper.getPid(), paper.getTitle(), paper.getEssabs(), paper.getKeywords().toString(), paper.getContentUrl(), paper.getAuths().toString(), paper.getField(), paper.getType(), paper.getTheme(), paper.getPublishDate(), paper.getDerivation(), paper.getRefs().toString(), paper.getFavTime(), paper.getRefTimes(), paper.getStats(), paper.getCategoryId());
//        esTool.addPaper(paperMapper);

        customResponse.setMessage("文章上传成功！");
        return customResponse;
    }

    /**
     * 修改文章信息
     * @param
     * @return
     */
    public CustomResponse updatePaper(Long pid,
                                      String title,
                                      String essabs,
                                      List<String> keywords,
                                      MultipartFile content,
                                      Map<String, Integer> auths,
                                      String field,
                                      String type,
                                      String theme,
                                      String publishDate,
                                      String derivation,
                                      List<Long> refs,
                                      Integer categoryId) {
        CustomResponse customResponse = new CustomResponse();

        // 保存文件到 OSS，返回URL
        String contentUrl = null;
        try {
            contentUrl = ossTool.uploadDocument(content);
        } catch (IOException e) {
            e.printStackTrace();
            customResponse.setCode(505);
            customResponse.setMessage("无法存储OSS");
            return customResponse;
        }

        if (contentUrl == null) {
            log.warn("OSS URL 为空，合并操作终止");
            customResponse.setMessage("无法生成文章url！");
            customResponse.setCode(500);
            return customResponse;
        }

        UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid);
        updateWrapper.setSql("title = '" + title + "', essabs = '" + essabs + "', keywords = '" + keywords.toString() + "', content_url = '" + contentUrl + "', auths = '" + auths.toString().replace("=", ":") + "', field = '" + field + "', type = '" + type + "', theme = '" + theme + "', publish_date = '" + publishDate + "', derivation = '" + derivation + "', refs = '" + refs.toString() + "', category_id = '" + categoryId + "'");

        paperMapper.update(null, updateWrapper);

        customResponse.setMessage("文章更新成功！");
        return customResponse;
    }
}
