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
        updateWrapper.setSql("fav_times = fav_times + 1");

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
        paperMapper.insertPaper(paper.getPid(), paper.getTitle(), paper.getEssAbs(), paper.getKeywords().toString(), paper.getContentUrl(), paper.getAuths().toString().replace("=", ":"), paper.getCategory(), paper.getType(), paper.getTheme(), paper.getPublishDate(), paper.getDerivation(), paper.getRefs().toString(), paper.getFavTimes(), paper.getRefTimes(), paper.getStats());
//        esTool.addPaper(paperMapper);

        customResponse.setMessage("文章上传成功！");
        return customResponse;
    }

    /**
     * 修改文章信息
     *
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
                                      List<Long> refs) {
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
        updateWrapper.setSql("title = '" + title + "', ess_abs = '" + essabs + "', keywords = '" + keywords.toString() + "', content_url = '" + contentUrl + "', auths = '" + auths.toString().replace("=", ":") + "', category = '" + field + "', type = '" + type + "', theme = '" + theme + "', publish_date = '" + publishDate + "', derivation = '" + derivation + "', refs = '" + refs.toString() + "'");

        paperMapper.update(null, updateWrapper);

        customResponse.setMessage("文章更新成功！");
        return customResponse;
    }
    /**
     * 查找用户收藏夹内所有文献
     *
     * @param fid 收藏夹id
     * @return CustomResponse
     */

    public CustomResponse getPaperByFid(Integer fid) {
        CustomResponse customResponse = new CustomResponse();
        String fidKey = "fid:" + fid;
        List<Map<String, Object>> papers = new ArrayList<>();
        Map<String, Object> paper;
        if (!redisTool.isExist(fidKey)) {
            customResponse.setCode(500);
            customResponse.setMessage("该收藏夹不存在");
        } else {
            Set<Object> paperSet = redisTool.zRange(fidKey, 0, -1);
            for (Object paperId : paperSet) {
                paper = paperMapper.getPaperByPid(Long.valueOf(paperId.toString()));
                papers.add(paper);
            }
            customResponse.setCode(200);
            customResponse.setMessage("获取成功");
            customResponse.setData(papers);
        }
        return customResponse;
    }

    @Override
    public CustomResponse modifyAuth(Long pid, String name, Integer uid) {
        // 创建更新条件
        UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid);

        // 查询文章
        Paper paper = paperMapper.selectOne(updateWrapper);

        if (paper == null) {
            return new CustomResponse(400, "文章不存在", null);
        }

        // 修改文章的作者信息
        paper.getAuths().put(name, uid);

        // 更新数据库中的文章信息
        int rowsAffected = paperMapper.update(paper, updateWrapper);

        if (rowsAffected > 0) {
            return new CustomResponse(200, "修改文章作者信息成功", null);
        } else {
            return new CustomResponse(500, "修改失败", null);
        }
    }

}
