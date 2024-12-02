package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.StormMapper;
import com.buaa01.illumineer_backend.service.StormService;
import com.buaa01.illumineer_backend.tool.StormTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class StormServiceImpl implements StormService {
    @Autowired
    private StormMapper stormMapper;
    @Autowired
    private StormTool storm;

    @Override
    public CompletableFuture<String> getStorm() {
        ArrayList<Paper> articles;
        String last_update = "updated_date=2024-01-01/";
//        String isNew = storm.check(last_update);
//        if (!isNew.equals(last_update)) {
//            last_update = isNew;
        articles = storm.getPapers(last_update);
//        }
//        System.out.println(articles.size());
        for (Paper article : articles)
            stormMapper.insertPaper(article);
        return CompletableFuture.completedFuture(Integer.toString(articles.size()));
    }
}