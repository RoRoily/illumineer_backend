package com.buaa01.illumineer_backend.service.impl.load;

import com.buaa01.illumineer_backend.entity.LoadMetrics;
import com.buaa01.illumineer_backend.service.LoadService;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

@Service
public class LoadServiceImpl implements LoadService {
    /**
     * 计算当前系统负载指数
     *
     * @return 负载指数
     */
    @Override
    public LoadMetrics getSystemLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        // 获取 CPU 使用率（需要结合操作系统支持）
        double systemLoad = osBean.getSystemLoadAverage(); // 系统的平均负载（1分钟）

        // 获取活跃线程数
        int activeThreads = threadBean.getThreadCount();

        // 获取 JVM 内存使用情况
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;

        // 计算负载指数（假设简单权重计算，按需调整）
        double loadIndex = normalize(systemLoad, 0, osBean.getAvailableProcessors()) * 0.5 +
                normalize(activeThreads, 0, 1000) * 0.3 +
                normalize(usedMemory, 0, Runtime.getRuntime().maxMemory()) * 0.2;

        // 返回负载指标
        return new LoadMetrics(systemLoad, activeThreads, usedMemory, loadIndex);
    }

    /**
     * 归一化计算 (0-1)
     */
    @Override
    public double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }
}
