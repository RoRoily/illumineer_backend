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
    public CustomResponse getRefTimes(int pid) {
        CustomResponse customResponse = new CustomResponse();
        Paper paper = null;
        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
//        paper = paperMapper.selectOne(queryWrapper);
        paper = paperMapper.getPaperByPid(pid);

        Map<String, Object> map = new HashMap<>();
        map.put("ref_times", paper.getRef_times());
        customResponse.setData(map);
        return customResponse;
    }

    /**
     * 根据 pid 增加引用量
     *
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
     *
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
        paperMapper.insert(paper);
//        esTool.addPaper(paperMapper);

        customResponse.setMessage("文章上传成功！");
        return customResponse;
    }

    /**
     * 更新作者（已认证）
     *
     * @param pid
     * @param aid
     * @return
     */
    public CustomResponse updateAuth(int pid, int aid) {
        CustomResponse customResponse = new CustomResponse();
//
//        if (getAuthorByAid() == null) { // 查找作者
//            customResponse.setMessage("该作者不存在");
//            return customResponse;
//        }
//
//        Paper paper = null;
//        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("pid", pid);
//        paper = paperMapper.getPaperByPid(pid);
//
//        Map<String, Integer> auths = paper.getAuths();
//        String author = getAuthorByAid().getAuthor();
//        if (auths.containsValue(aid)) { // 存在此作者
//            // 存在此作者，删除
//            auths.remove(author);
//            paper.setAuths(auths);
//        } else { // 不存在此作者
//            // 将该作者加入pid的作者列表中
//            auths.put(author, aid);
//            paper.setAuths(auths);
//        }
//
//        // 更新数据库
//        UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.eq("pid", pid);
//        updateWrapper.setSql("auths = " + auths);
//        paperMapper.update(null, updateWrapper);
//
        return customResponse;
    }

    /**
     * 更新作者（已认证）
     *
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
     *
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
                                      List<String> keywords,
                                      MultipartFile content,
                                      Map<String, Integer> auths,
                                      List<String> field,
                                      String type,
                                      String theme,
                                      Date publishDate,
                                      String derivation,
                                      List<Integer> refs) {
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
        updateWrapper.setSql("title = " + title + ", essAbs = " + essAbs + ", keywords = " + keywords + ", contentUrl = " + contentUrl + ", auths = " + auths + ", field = " + field + ", type = " + type + ", theme = " + theme + ", publishDate = " + publishDate + ", derivation = " + derivation + ", refs = " + refs);

        paperMapper.update(null, updateWrapper);

        customResponse.setMessage("文章更新成功！");
        return customResponse;
    }

    @Override
    public CustomResponse updatePaperAdoptionStatus(String name, List<Integer> pidsForAdopt){
        String key = "AdoptObject:"+name;
        //可能需要更改的文章对象
        Set<Object> papers = redisTool.getSetMembers(key);
        if(papers == null){
            for(Integer pid : pidsForAdopt){
                QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("pid", pid);
                Paper Adopaper = paperMapper.getPaperByPid(pid);
                //修改Auths的相关值和属性
                Adopaper.getAuths().put(name,userClientService.getCurrentUser().getUid());
            }
        }
        else{
            for(Object paperEntity : papers){
                Paper p = (Paper) paperEntity;
                if(pidsForAdopt.contains(p.getPid())) p.getAuths().put(name,userClientService.getCurrentUser().getUid());
            }
        }
        CustomResponse customResponse = new CustomResponse(200,"Success to adopt",null);
        return customResponse;
    }
}
