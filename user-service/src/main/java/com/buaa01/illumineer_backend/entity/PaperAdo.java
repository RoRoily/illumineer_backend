package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

//生成文献的认领条目
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperAdo {
    @TableId(type = IdType.INPUT)
    private Long pid;
    // 文章的相关信息
    private String title; // 文章题目
    private Map<String, Integer> auths; // 文章作者
    private Date publishDate; // 出版时间
    private Integer stats; // 0 正常 1 已删除
    boolean hasBeenAdoptedByTheAuth; //已经被别人认领了！

    public PaperAdo(Paper paper, String name){
       PaperAdo paperAdo = new PaperAdo();
       paperAdo.pid = paper.getPid();
       paperAdo.title = paper.getTitle();
       paperAdo.auths = paper.getAuths();
       paperAdo.publishDate = paper.getPublishDate();
       paperAdo.stats = paper.getStats();
       paperAdo.hasBeenAdoptedByTheAuth = false;
       //已经被认领
       if(paper.getAuths().get(name)!=0)paperAdo.hasBeenAdoptedByTheAuth = true;
    }
}
