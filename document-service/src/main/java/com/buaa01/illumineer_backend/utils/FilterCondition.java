package com.buaa01.illumineer_backend.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Map;

import com.alibaba.fastjson.JSON;

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

    @SuppressWarnings("unchecked")
    public FilterCondition(Map<String, Object> sc) { // 构造函数
        // 若无则赋值为空
        if (sc.get("year") == null) {
            this.year = new ArrayList<>();
        } else {
            this.year = (ArrayList<String>) sc.get("year");
        }

        if (sc.get("derivation") == null) {
            this.derivation = new ArrayList<>();
        } else {
            this.derivation = (ArrayList<String>) sc.get("derivation");
        }

        if (sc.get("type") == null) {
            this.type = new ArrayList<>();
        } else {
            this.type = (ArrayList<String>) sc.get("type");
        }

        if (sc.get("theme") == null) {
            this.theme = new ArrayList<>();
        } else {
            this.theme = (ArrayList<String>) sc.get("theme");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof FilterCondition) {
            FilterCondition sc = (FilterCondition) obj;
            return year.equals(sc.year) && derivation.equals(sc.derivation) && type.equals(sc.type)
                    && theme.equals(sc.theme);
        } else {
            return false;
        }
    }

}
