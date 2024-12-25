package com.buaa01.illumineer_backend.service;


import com.buaa01.illumineer_backend.entity.LoadMetrics;


public interface LoadService {

    /**
     * 计算当前系统负载指数
     *
     * @return 负载指数
     */
     LoadMetrics getSystemLoad();
    /**
     * 归一化计算 (0-1)
     */
     double normalize(double value, double min, double max);
    /**
     * 负载指标数据类
     */
}
