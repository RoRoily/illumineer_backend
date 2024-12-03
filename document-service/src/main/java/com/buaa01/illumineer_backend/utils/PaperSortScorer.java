package com.buaa01.illumineer_backend.utils;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.List;

import com.buaa01.illumineer_backend.entity.SearchResultPaper;

public class PaperSortScorer {
    /* 引用次数权重 */
    final static double W_REF = 0.3;
    /* 收藏次数权重 */
    final static double W_FAV = 0.2;
    /* 出版时间权重 */
    final static double W_TIME = 0.1;
    /* 关键词匹配权重 */
    final static double W_KEYWORD = 0.4;

    public static double calculateScore(SearchResultPaper paper, List<String> userKeywords) {
        // 引用次数评分
        double refScore = normalize(paper.getRefTimes(), 1000);
        // 收藏次数评分
        double favScore = normalize(paper.getFavTime(), 10000);
        // 时间评分
        double timeScore = calculateTimeScore((Date) paper.getPublishDate());
        // 关键词匹配度评分
        double keywordScore = calculateKeywordScore(paper.getKeywords(), userKeywords);

        // 综合评分
        return W_REF * refScore + W_FAV * favScore + W_TIME * timeScore + W_KEYWORD * keywordScore;
    }

    /**
     * 
     * @param value       分子
     * @param denominator 分母
     * @return score
     */
    private static double normalize(int value, int denominator) {
        return (double) value / denominator;
    }

    /**
     * 计算时间评分
     * 
     * @param publishDate 发布日期
     * @return score
     */
    private static double calculateTimeScore(Date publishDate) {
        long yearsSincePublished = ChronoUnit.YEARS.between((Temporal) publishDate, LocalDate.now());
        double lambda = 0.1; // 时间衰减系数
        return Math.exp(-lambda * yearsSincePublished);
    }

    /**
     * 关键词匹配分数，匹配到的越多分数越高
     * 
     * @param paperKeywords
     * @param userKeywords
     * @return
     */
    private static double calculateKeywordScore(List<String> paperKeywords, List<String> userKeywords) {
        long matches = paperKeywords.stream().filter(userKeywords::contains).count();
        return (double) matches / paperKeywords.size();
    }
}