package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User2Paper {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    private Integer pid;

    private Integer weight; // 推荐使用的权重：点击+1，下载+2，收藏+3
    private Integer collect; //收藏 0未收藏 1已收藏
    //    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date acessDate;

    public User2Paper(Integer id, Integer uid, Integer pid, Integer collect, Date acessDate) {
        this.id = id;
        this.uid = uid;
        this.pid = pid;
        this.collect = collect;
        this.acessDate = acessDate;
        weight = 1;
    }

    public void updateWeight(Integer addWeight) {
        weight += addWeight;
    }
}
