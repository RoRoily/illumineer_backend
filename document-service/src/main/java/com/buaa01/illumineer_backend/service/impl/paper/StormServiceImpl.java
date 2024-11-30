package com.buaa01.illumineer_backend.service.impl.paper;

import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.StormMapper;
import com.buaa01.illumineer_backend.service.paper.StormService;
import com.buaa01.illumineer_backend.tool.StormTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class StormServiceImpl implements StormService {
    @Autowired
    private StormMapper stormMapper;
    @Autowired
    private StormTool storm;

    @Override
    public String getStorm() {
        ArrayList<Paper> articles = new ArrayList<>();
        String last_update = "updated_date=2024-11-25/";
        String isNew = storm.check(last_update);
        if (!isNew.equals(last_update)) {
            last_update = isNew;
            articles = storm.getPapers(last_update);
        }
        for (Paper article : articles)
            stormMapper.insertPaper(article);
        return Integer.toString(articles.size());
    }
}