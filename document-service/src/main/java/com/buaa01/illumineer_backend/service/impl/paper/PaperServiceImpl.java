package com.buaa01.illumineer_backend.service.impl.paper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class PaperServiceImpl implements PaperService {

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private PaperMapper paperMapper;

    @Override
    public CustomResponse getPaperByPid(Integer pid) {
        CustomResponse customResponse = new CustomResponse();
        Paper paper = null;
        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
//        paper = paperMapper.selectOne(queryWrapper);
        paper = paperMapper.getPaperByPid(pid);

        Map<String, Object> map = new HashMap<>();
        map.put("title",paper.getTitle());
        map.put("essAbs", paper.getEssAbs());
        map.put("keywords",paper.getKeywords());
        map.put("contentUrl",paper.getContentUrl());
        map.put("auths",paper.getAuths());
        map.put("field",paper.getField());
        map.put("type",paper.getType());
        map.put("theme",paper.getTheme());
        map.put("publishDate",paper.getPublishDate());
        map.put("derivation",paper.getDerivation());
        map.put("ref_times", paper.getRef_times());
        map.put("fav_times",paper.getFav_time());
        map.put("refs",paper.getRefs());
        customResponse.setData(map);
        return customResponse;
    }

    // 一框式检索：模糊查询 + 排序 + 分页
    @Override
    public CustomResponse searchPapers(String keyword, Integer offset, Integer sortType) {
        List<Paper> papers = null;

        // 1. 模糊搜索：keyword
        papers = searchByKeyword(keyword);

        // 2. searchbByOrder 对搜索结果进行排序：sortType
        papers = searchByOrder(papers, sortType);

        // 3. searchByPage 对排序结果进行分页，并将当前页 offset 需要的内容返回
        papers = searchByPage(papers, 20, offset);

        // 4. 返回结果
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(papers);
        return customResponse;
    }

    /**
     * 模糊查询
     * @param keyword 搜索内容
     * @return 文献信息
     */
    List<Paper> searchByKeyword(String keyword) {
        try {
            List<Paper> list = new ArrayList<>();
            Query query = Query.of(q -> q.multiMatch(m -> m.fields("title", "essAbs", "keywords", "field").query(keyword)));
            SearchRequest searchRequest = new SearchRequest.Builder().index("paper").query(query).build();
            SearchResponse<Paper> searchResponse = client.search(searchRequest, Paper.class);
            for (Hit<Paper> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    list.add(hit.source());
                }
            }
            return list;
        } catch (IOException e) {
            log.error("查询ES相关文献文档时出错了：" + e);
            return Collections.emptyList();
        }
    }

    /**
     * 分页
     * @param papers 搜索的结果
     * @param pageNum 一页的条目数量
     * @param offset 第几页
     * @return 文献信息
     */
    List<Paper> searchByPage(List<Paper> papers, Integer pageNum, Integer offset) {
        if (offset == null || offset == 0) {
            offset = 1;
        }
        if (pageNum == null || pageNum == 0) {
            pageNum = 20;
        }
        int startIndex = (offset - 1) * pageNum;
        int endIndex = startIndex + pageNum;
        // 检查数据是否足够满足分页查询
        if (startIndex > papers.size()) {
            // 如果数据不足以填充当前分页，返回空列表
            return Collections.emptyList();
        }
        endIndex = Math.min(endIndex, papers.size());
        List<Paper> sublist = papers.subList(startIndex, endIndex);
        if (sublist.isEmpty()) {
            return Collections.emptyList();
        }
//        List<Map<String, Object>> mapList = new ArrayList<>();
//        for(Paper paper : papers) {
//            Map<String, Object> map = getPaperMap(paper);
//            mapList.add(map);
//        }
//        return mapList;
        return sublist;
    }

    /**
     * 排序
     * @param papers 搜索的结果
     * @param sortType 根据这个来进行排序 // 1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @return 文献信息
     */
    List<Paper> searchByOrder(List<Paper> papers, Integer sortType) {
        papers.sort(new Comparator<Paper>() {
            @Override
            public int compare(Paper p1, Paper p2) {
                if (sortType == 1) {
                    return p2.getPublishDate().compareTo(p1.getPublishDate());
                } else if (sortType == 2) {
                    return p2.getRef_times() - p1.getRef_times();
                } else if (sortType == 3) {
                    return p2.getFav_time() - p1.getFav_time();
                }
                return 0;
            }
        });
        return papers;
    }

    /**
     * 根据 pid 返回引用量
     * @param pid 文章 id
     * @return 引用量
     */
    public CustomResponse getRefTimes(int pid) {
        CustomResponse customResponse = new CustomResponse();
        Paper paper = null;
        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
//        paper = paperMapper.selectOne(queryWrapper);
        paper = paperMapper.getPaperByPid(pid);

        Map<String, Object> map = new HashMap<>();
        map.put("ref_times",paper.getRef_times());
        customResponse.setData(map);
        return customResponse;
    }

    /**
     * 根据 pid 增加引用量
     * @param pid 文章 id
     */
    public CustomResponse addRefTimes(int pid) {
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
     * @param pid 文章 id
     */
    public CustomResponse addFavTimes(int pid) {
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
     * @param paper 文章
     * @param content 文章内容（文件）
     */
    public CustomResponse uploadPaper(Paper paper, MultipartFile content) {
        CustomResponse customResponse = new CustomResponse();

        // 保存文件到 OSS，返回URL
        String contentUrl = ossTool.uploadPaperContent(content, "content");
        if (contentUrl == null) {
            log.warn("OSS URL 为空，合并操作终止");
            customResponse.setMessage("无法生成文章url！");
            customResponse.setCode(500);
            return customResponse;
        }

        // 将文章信息封装
        paper.setContentUrl(contentUrl);

        // 存入数据库
        paperMapper.insert(paper);
        esTool.addPaper(paperMapper);

        customResponse.setMessage("文章上传成功！");
        return customResponse;
    }

    /**
     * 更新作者（已认证）
     * @param pid
     * @param aid
     * @return
     */
    public CustomResponse updateAuth(int pid, int aid) {
        CustomResponse customResponse = new CustomResponse();

        if (getAuthorByAid() == null) { // 查找作者
            customResponse.setMessage("该作者不存在");
            return customResponse;
        }

        Paper paper = null;
        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
        paper = paperMapper.getPaperByPid(pid);

        Map<String, Integer> auths = paper.getAuths();
        String author = getAuthorByAid().getAuthor();
        if (auths.containsValue(aid)) { // 存在此作者
            // 存在此作者，删除
            auths.remove(author);
            paper.setAuths(auths);
        } else { // 不存在此作者
            // 将该作者加入pid的作者列表中
            auths.put(author, aid);
            paper.setAuths(auths);
        }

        // 更新数据库
        UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid);
        updateWrapper.setSql("auths = " + auths);
        paperMapper.update(null, updateWrapper);

        return customResponse;
    }

    /**
     * 更新作者（已认证）
     * @param pid
     * @param author
     * @return
     */
    public CustomResponse updateAuth(int pid, String author) {
        CustomResponse customResponse = new CustomResponse();

        Paper paper = null;
        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
        paper = paperMapper.getPaperByPid(pid);

        Map<String, Integer> auths = paper.getAuths();
        if (auths.get(author) != null) { // 存在此作者
            // 存在此作者，删除
            auths.remove(author);
            paper.setAuths(auths);
        } else { // 不存在此作者
            // 将该作者加入pid的作者列表中
            auths.put(author, null);
            paper.setAuths(auths);
        }

        // 更新数据库
        UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid);
        updateWrapper.setSql("auths = " + auths);
        paperMapper.update(null, updateWrapper);

        return customResponse;
    }

    /**
     * 删除文章
     * @param pid
     * @return
     */
    public CustomResponse deletePaper(int pid) {
        CustomResponse customResponse = new CustomResponse();
        UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid);
        updateWrapper.setSql("stats = 1");

        paperMapper.update(null, updateWrapper);

        customResponse.setMessage("文章删除成功！");
        return customResponse;
    }

    /**
     * 修改文章信息
     * @param
     * @return
     */
    public CustomResponse updatePaper(int pid,
                               String title,
                               String essAbs,
                               String keywords,
                               MultipartFile content,
                               String field,
                               String type,
                               String theme,
                               Date publishDate,
                               String derivation) {
        CustomResponse customResponse = new CustomResponse();

        // 保存文件到 OSS，返回URL
        String contentUrl = ossTool.uploadPaperContent(content, "content");
        if (contentUrl == null) {
            log.warn("OSS URL 为空，合并操作终止");
            customResponse.setMessage("无法生成文章url！");
            customResponse.setCode(500);
            return customResponse;
        }

        UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid);
        updateWrapper.setSql("title = " + title + ", essAbs = " + essAbs + ", contentUrl = " + contentUrl + ", type = " + type + ", theme = " + theme + ", publishDate = " + publishDate + ", derivation = " + derivation);

        paperMapper.update(null, updateWrapper);

        customResponse.setMessage("文章更新成功！");
        return customResponse;
    }
}
