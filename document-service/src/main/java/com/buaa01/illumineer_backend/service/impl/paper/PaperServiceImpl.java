package com.buaa01.illumineer_backend.service.impl.paper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.SearchResultPaper;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.client.UserClientService;
import com.buaa01.illumineer_backend.service.paper.PaperService;
import com.buaa01.illumineer_backend.tool.OssTool;
import com.buaa01.illumineer_backend.tool.RedisTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
     * 推荐
     * @param num 数据条数
     */
    public CustomResponse getRecommend(Integer num) {
        CustomResponse customResponse = new CustomResponse();
        List<Long> pids = paperMapper.getRecommend(num);
        List<Map<String, Object>> papers = new ArrayList<>();
        for (Long pid : pids) {
            Map<String, Object> searchPaper = paperMapper.getPaperByPid(pid);
            Map<String, Object> paper = new HashMap<>();
            paper.put("pid", searchPaper.get("pid"));
            paper.put("title", searchPaper.get("title"));
            paper.put("keywords", searchPaper.get("keywords"));
            paper.put("auths", searchPaper.get("auths"));
            paper.put("derivation", searchPaper.get("derivation"));
            paper.put("publishDate", searchPaper.get("publishDate"));
            paper.put("refTimes", searchPaper.get("refTimes"));
            paper.put("favTimes", searchPaper.get("favTimes"));
            paper.put("type", searchPaper.get("type"));
            paper.put("theme", searchPaper.get("theme"));
            paper.put("contentUrl", searchPaper.get("contentUrl"));
            paper.put("category", searchPaper.get("category"));
            paper.put("essAbs", searchPaper.get("ess_abs"));
            papers.add(paper);
        }
        customResponse.setData(papers);
        return customResponse;
    }

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
    public CustomResponse uploadPaper(Paper paper, MultipartFile content, Integer uid) {
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

        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", paper.getTitle());
        List<Paper> searchPaper = paperMapper.selectList(queryWrapper);
        if (searchPaper == null || searchPaper.isEmpty()) {

            // 存入数据库
            paperMapper.insertPaper(paper.getPid(), paper.getTitle(), paper.getEssAbs(), paper.getKeywords().toString(), paper.getContentUrl(), paper.getAuths().toString().replace("=", ":"), paper.getCategory(), paper.getType(), paper.getTheme(), paper.getPublishDate(), paper.getDerivation(), paper.getRefs().toString(), paper.getFavTimes(), paper.getRefTimes(), paper.getStats());
            searchPaper = paperMapper.selectList(queryWrapper);
            Paper newPaper = searchPaper.get(0);
            //在redis将pid与用户绑定
            redisTool.addSetMember("property:" + uid, newPaper.getPid());
            redisTool.addSetMember("paperBelonged:" + newPaper.getPid(), uid);
//        esTool.addPaper(paperMapper);
            customResponse.setMessage("文章上传成功！");
        } else {
            customResponse.setCode(505);
            customResponse.setMessage("该文献已在数据库中");
        }
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

        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", title);
        Paper paper = paperMapper.selectOne(queryWrapper);
        if (paper == null) {
            UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("pid", pid);
            updateWrapper.setSql("title = '" + title + "', ess_abs = '" + essabs + "', keywords = '" + keywords.toString() + "', content_url = '" + contentUrl + "', auths = '" + auths.toString().replace("=", ":") + "', category = '" + field + "', type = '" + type + "', theme = '" + theme + "', publish_date = '" + publishDate + "', derivation = '" + derivation + "', refs = '" + refs.toString() + "'");

            paperMapper.update(null, updateWrapper);

            customResponse.setMessage("文章更新成功！");
        } else {
            customResponse.setCode(505);
            customResponse.setMessage("该文献已在数据库中");
        }
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

                ObjectMapper objectMapper = new ObjectMapper();
                List<String> keywords = null;
                Map<String, Integer> auths = null;
                try {
                    // keywords 的转换
                    keywords = objectMapper.readValue(paper.get("keywords").toString(),
                            new TypeReference<List<String>>() {
                            });
                    paper.put("keywords", keywords);

                    // auths 的转换
                    auths = objectMapper.readValue(paper.get("auths").toString(),
                            new TypeReference<Map<String, Integer>>() {
                            });
                    paper.put("auths", auths);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Date date;
                // 判断是否是 ISO 格式，转换date格式
                if (!paper.get("publish_date").toString().contains(" ")) {
                    date = Date.from(
                            LocalDateTime.parse(paper.get("publish_date").toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                    .atZone(ZoneId.systemDefault()).toInstant());
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                    date = Date.from(LocalDateTime.parse(paper.get("publish_date").toString(), formatter)
                            .atZone(ZoneId.systemDefault()).toInstant());
                }

                Map<String, Object> searchResultPaper = new HashMap<>();
                searchResultPaper.put("pid", paper.get("pid"));
                searchResultPaper.put("title", paper.get("title"));
                searchResultPaper.put("keywords", keywords);
                searchResultPaper.put("auths", auths);
                searchResultPaper.put("category", paper.get("category"));
                searchResultPaper.put("type", paper.get("type"));
                searchResultPaper.put("theme", paper.get("theme"));
                searchResultPaper.put("publish_date", date);
                searchResultPaper.put("derivation", paper.get("derivation"));
                searchResultPaper.put("ref_times", Integer.parseInt(paper.get("ref_times").toString()));
                searchResultPaper.put("fav_times", Integer.parseInt(paper.get("fav_times").toString()));
                searchResultPaper.put("content_url", paper.get("content_url"));

                papers.add(searchResultPaper);
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


        Map<String, Object> paper = paperMapper.getPaperByPid(pid);

        ObjectMapper objectMapper = new ObjectMapper();
        try {

            // auths 的转换
            Map<String, Integer> auths = objectMapper.readValue(paper.get("auths").toString(), new TypeReference<Map<String, Integer>>() {
            });
            paper.put("auths", auths);
            auths.put(name, uid);


            // 将修改后的 auths 写回 paper
            paper.put("auths", objectMapper.writeValueAsString(auths));  // 将 Map 转换为 JSON 字符串

            // 3. 使用 MyBatis 更新数据
            int rowsAffected = paperMapper.updatePaper(pid, paper);  // 假设你有一个方法可以更新数据库中的文章

            if (rowsAffected > 0) {
                return new CustomResponse(200, "修改文章作者信息成功", null);
            } else {
                return new CustomResponse(500, "修改失败", null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (paper == null) {
            return new CustomResponse(400, "文章不存在", null);
        }
        System.out.println(paper);
        // 修改文章的作者信息
        //paper.getAuths().put(name, uid);

        // 更新数据库中的文章信息
        //int rowsAffected = paperMapper.update(paper, updateWrapper);

        return new CustomResponse();
    }

}
