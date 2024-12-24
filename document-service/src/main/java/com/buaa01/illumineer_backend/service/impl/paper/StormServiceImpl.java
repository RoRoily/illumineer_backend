package com.buaa01.illumineer_backend.service.impl.paper;

import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.StormMapper;
import com.buaa01.illumineer_backend.service.StormService;
import com.buaa01.illumineer_backend.tool.StormTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class StormServiceImpl implements StormService {
    @Autowired
    private StormMapper stormMapper;
    @Autowired
    private StormTool storm;

    @Async
    @Override
    public CompletableFuture<String> getStorm() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
        int articles = 0;
        String last_update = "2024-11-25";
        String isNew = storm.check();
        if (!isNew.equals(last_update)) {
            last_update = isNew;
            articles = storm.getPapers(last_update);
        }
//        for (Paper article : articles)
//            stormMapper.insertPaper(article.getPid(), article.getTitle(), article.getEssAbs(), article.getKeywords(), article.getContentUrl(), article.getAuths(), article.getCategory(), article.getType(), article.getTheme(), article.getPublishDate(), article.getDerivation(), article.getRefs(), article.getRefTimes(), article.getRefTimes(), article.getStats());
        return CompletableFuture.completedFuture(String.valueOf(articles));
    }
}