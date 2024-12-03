package com.buaa01.illumineer_backend.service.impl.paper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.PaperAdo;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperAdoptionService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
}
