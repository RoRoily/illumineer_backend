package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultPaper {
    private Integer pid;
    // 文章的相关信息
    private String title; // 文章题目
//    private String essAbs; // 文章摘要
    private List<String> keywords;
    private Map<String, Integer> auths; // 文章作者
    private List<String> field; // 相关领域
    private String type; // 文章类型 (期刊、论文、会议、报纸) // sql add
    private String theme; // 文章主题 // sql add
    private Date publishDate; // 出版时间
    private String derivation;// 来源
    private Integer ref_times; // 引用次数
    private Integer fav_time; // 收藏次数
    private List<Integer> refs; // 引用文献
    private Integer stats; // 0 正常 1 已删除

    public List<String> category2String(Paper paper){
        List<String> cons = new ArrayList<>();
        for(Category category: paper.getField()){
            cons.add(category.getSubClassName());
        }
        return cons;
    }
}
