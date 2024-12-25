package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.buaa01.illumineer_backend.handler.MapTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    @TableField(typeHandler = MapTypeHandler.class)
    private Map<String, Integer> auths; // 文章作者
    private Date publishDate; // 出版时间
    private Integer stats; // 0 正常 1 已删除
    boolean hasBeenAdoptedByTheAuth; //已经被别人认领了！

    public PaperAdo setNewPaperAdo(Map<String, Object> paper, String name) {
        PaperAdo paperAdo = new PaperAdo();
        paperAdo.pid = (Long) paper.get("pid");
        paperAdo.title = (String) paper.get("title");
        paperAdo.auths = (Map<String, Integer>) paper.get("auths");
        LocalDateTime localDateTime = (LocalDateTime) paper.get("publish_date"); // 你的 LocalDateTime 对象
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        paperAdo.publishDate = Date.from(instant);
        paperAdo.stats = (Boolean) paper.get("stats") ? 1 : 0;
        paperAdo.hasBeenAdoptedByTheAuth = false;

        //已经被认领
//        if (paper.getAuths().get(name) != 0) paperAdo.hasBeenAdoptedByTheAuth = true;
        return paperAdo;
    }
}