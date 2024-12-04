package com.buaa01.illumineer_backend.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Map;

/**
 * 用以存储筛选条件，方便传递参数
 * 
 */
@Getter
public class FilterCondition {
    private ArrayList<String> year; // 年份
    private ArrayList<String> derivation; // 来源
    private ArrayList<String> type; // 类型
    private ArrayList<String> theme; // 主题

    public FilterCondition(Map<String, ArrayList<String>> sc) { // 构造函数
        this.year = sc.get("year");
        this.derivation = sc.get("derivation");
        this.type = sc.get("type");
        this.theme = sc.get("theme");

        // 若无则赋值为空
        if (year == null) {
            this.year = new ArrayList<>();
        }
        if (derivation == null) {
            this.derivation = new ArrayList<>();
        }
        if (type == null) {
            this.type = new ArrayList<>();
        }
        if (theme == null) {
            this.theme = new ArrayList<>();
        }
    }

}
