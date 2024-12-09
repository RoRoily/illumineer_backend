package com.buaa01.illumineer_backend.service.impl.paper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.*;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperAdoptionService;
import com.buaa01.illumineer_backend.service.paper.PaperSearchService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class PaperAdoptionServiceImpl implements PaperAdoptionService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private RedisTool redisTool;

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /***
     * 根据作者姓名返回包含该姓名的认领条目列表
     * @param name 姓名
     * **/
    @Override
    public CustomResponse getPaperAdoptionsByName(String name){
        CustomResponse customResponse = new CustomResponse();

        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        List<Paper> papers = paperMapper.selectList(queryWrapper);
        List<PaperAdo> paperAdos = new ArrayList<>();

        for (Paper paper: papers) {
            Map<String, Integer> auths = paper.getAuths();
            if (auths.get(name) != null) {
                PaperAdo paperAdo = new PaperAdo();
                paperAdo = paperAdo.setNewPaperAdo(paper, name);
                paperAdos.add(paperAdo);
                // 缓存
                CompletableFuture.runAsync(() -> {
                    redisTool.setExObjectValue("AdoptObject:" + name, paper);    // 异步更新到redis
                }, taskExecutor);
            }
        }

        customResponse.setData(paperAdos);
        return customResponse;
    }

    /***
     * 根据pids中的各个pid找到Paper，转换成PaperAdo并返回
     * @param pids
     * **/
    @Override
    public List<PaperAdo> getPaperAdoptionsByList(List<Long> pids) {
        List<PaperAdo> paperAdos = new ArrayList<>();
        for (Long pid : pids) {
            Paper paper = null;
            QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("pid", pid);
            paper = paperMapper.selectOne(queryWrapper);

            PaperAdo paperAdo = new PaperAdo(pid, paper.getTitle(), paper.getAuths(), paper.getPublishDate(), paper.getStats(), false);
            paperAdos.add(paperAdo);
        }
        return paperAdos;
    }

    /***
     * 根据category返回该category的认领条目列表
     * @param category
     * @param total 总数
     * **/
    public List<PaperAdo> getPaperAdoptionsByCategory(Category category, Integer total) {
        List<PaperAdo> paperAdos = new ArrayList<>();
        List<Paper> papers = null;
        List<Long> pids = new ArrayList<>();
        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", category.getMainClassId());
        papers = paperMapper.selectList(queryWrapper);
        for (Paper paper: papers) {
            pids.add(paper.getPid());
        }
        paperAdos = getPaperAdoptionsByList(pids);
        paperAdos = paperAdos.subList(0, total);

        return paperAdos;
    }
}
