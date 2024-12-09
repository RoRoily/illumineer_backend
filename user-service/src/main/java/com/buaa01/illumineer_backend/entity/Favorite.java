package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Favorite {
    // 用户的默认收藏夹fid = uid
    private Integer fid; // 收藏夹ID
    private Integer uid; // 所属用户ID
    private Integer type; // 收藏夹类型 1默认收藏夹 2用户创建
    private String title; // 收藏夹名称
    private Integer count; // 收藏夹中文章数量
    private Integer isDelete; // 是否删除 1已删除

    /**
     * 更新收藏夹中文章数量
     * 
     * @param mod 1增加 0减少
     */
    public void updataFavCounts(Integer mod) {
        if (mod == 1) {
            count++;
        } else {
            count--;
        }
    }
}
