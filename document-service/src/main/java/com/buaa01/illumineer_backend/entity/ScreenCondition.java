package com.buaa01.illumineer_backend.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Map;

/**
 * 用以存储筛选条件，方便传递参数
 * 
 */
public class ScreenCondition {
    private ArrayList<String> year; // 年份
    private ArrayList<String> derivation; // 来源
    private ArrayList<String> type; // 类型
    private ArrayList<String> theme; // 主题
    // add more…

    public ScreenCondition(Map<String, ArrayList<String>> sc) { // 构造函数
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

    public ArrayList<String> getYear() {
        return this.year;
    }

    public ArrayList<String> getDerivation() {
        return this.derivation;
    }

    public ArrayList<String> getType() {
        return this.type;
    }

    public ArrayList<String> getTheme() {
        return this.theme;
    }
}
