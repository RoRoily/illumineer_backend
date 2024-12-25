package com.buaa01.illumineer_backend.service.impl.search;

import com.buaa01.illumineer_backend.service.search.SearchService;
import com.buaa01.illumineer_backend.tool.ESTool;
import com.buaa01.illumineer_backend.tool.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RedisTool redisTool;

    @Autowired
    private ESTool esTool;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    public String addSearchWord(String text) {
        // 处理传入的内容 去除特殊字符
        String formattedString = formatString(text);
        // 如果格式化后的字符串没有任何中英日文或数字，直接返回null
        if (formattedString.length() == 0) return null;
        // 判断剩余中英日文是否达到可存为关键词的长度
        int keywordLength = countChineseAndLetters(formattedString);
        // 如果长度不符合就直接返回格式化字符串，不存redis和ES
        if (keywordLength < 2 || keywordLength > 30) return formattedString;
        // 查询数据库中是否有该词条，用异步线程不耽误后面查询
        CompletableFuture.runAsync(() -> {
            if (redisTool.isExistInZSet("search_word", formattedString)) {
                // 如果有，就热度加一
                redisTool.incrementZScore("search_word", formattedString, 1);
            } else {
                // 否则添加成员到redis和ES
                redisTool.storeZSetByScore("search_word", formattedString, 1);
                esTool.addSearchWord(formattedString);
            }
        }, taskExecutor);
        return formattedString;
    }

    @Override
    public List<String> getMatchingWord(String keyword) {
        return esTool.getMatchingWord(keyword);
    }



//    @Override
//    public List<Long> getCount(String keyword) {
//        // 提交视频数量查询任务
//        CompletableFuture<Long> videoFuture = CompletableFuture.supplyAsync(() -> esUtil.getVideoCount(keyword, true), taskExecutor);
//        // 提交用户数量查询任务
//        CompletableFuture<Long> userFuture = CompletableFuture.supplyAsync(() -> esUtil.getUserCount(keyword), taskExecutor);
//        // 组合两个CompletableFuture以特定顺序执行
//        CompletableFuture<List<Long>> combinedFuture = videoFuture.thenCombine(userFuture, (videoCount, userCount) -> {
//            List<Long> result = new ArrayList<>();
//            result.add(videoCount);
//            result.add(userCount);
//            return result;
//        });
//        try {
//            return combinedFuture.get(); // 等待线程全部执行完成
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//            List<Long> list = new ArrayList<>();
//            list.add(0L);
//            list.add(0L);
//            return list;
//        }
//    }

    /**
     * 格式化包含特殊字符的字符串
     */
    public String formatString(String input) {
        // 使用正则表达式替换特殊字符，并保留一个空格符
        String formattedString = input.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fffぁ-んァ-ヶ]+", " ");
        // 去除首尾空格
        formattedString = formattedString.trim();
        return formattedString;
    }

    /**
     * 计算格式化后的中文和字母数量
     */
    public int countChineseAndLetters(String formattedString) {
        // 去除数字和空格，计算剩余字符中中文和字母的数量
        String filteredString = formattedString.replaceAll("[0-9\\s]+", "");
        return filteredString.length();
    }

}

