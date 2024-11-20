package com.buaa01.illumineer_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElasticSearchPaper {
    private Integer pid;
    /** 文章题目 */
    private String title;
}
