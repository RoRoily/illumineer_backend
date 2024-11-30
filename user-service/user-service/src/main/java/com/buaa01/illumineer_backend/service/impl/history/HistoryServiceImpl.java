package com.buaa01.illumineer_backend.service.impl.history;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.DTO.HistoryDTO;
import com.buaa01.illumineer_backend.mapper.HistoryMapper;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.service.history.HistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class HistoryServiceImpl implements HistoryService {
    @Autowired
    private HistoryMapper historyMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 分页返回历史记录中的条目
     * @param set 查询结果的集合
     * @param quantity 需要查询的数量
     * @oaram index 搜索的偏差z
     * 并行处理
     *
     */
    @Override
    public List<Map<String, Object>> getHistoryByPage(Set<Object> set, Integer quantity, Integer index){
        if (index == null) {
            index = 1;
        }
        if (quantity == null) {
            quantity = 10;
        }
        int startIndex = (index - 1) * quantity;
        int endIndex = startIndex + quantity;
        // 检查数据是否足够满足分页查询
        if (startIndex > set.size()) {
            // 如果数据不足以填充当前分页，返回空列表
            return Collections.emptyList();
        }
        // 使用线程安全的集合类 CopyOnWriteArrayList 保证多线程处理共享List不会出现并发问题
        List<HistoryDTO> videoList = new CopyOnWriteArrayList<>();

        // 直接数据库分页查询    （平均耗时 13ms）
        List<Object> idList = new ArrayList<>(set);
        endIndex = Math.min(endIndex, idList.size());
        List<Object> sublist = idList.subList(startIndex, endIndex);
        QueryWrapper<HistoryDTO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("vid", sublist).ne("status", 3);
        videoList = videoMapper.selectList(queryWrapper);
        if (videoList.isEmpty()) return Collections.emptyList();

        // 并行处理每一个视频，提高效率
        // 先将videoList转换为Stream
        Stream<Video> videoStream = videoList.stream();
        List<Map<String, Object>> mapList = videoStream.parallel() // 利用parallel()并行处理
                .map(video -> {
//                    long start = System.currentTimeMillis();
//                    System.out.println("================ 开始查询 " + video.getVid() + " 号视频相关信息 ===============   当前时间 " + start);
                    Map<String, Object> map = new HashMap<>();
                    map.put("video", video);

                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                        map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
                    }, taskExecutor);

                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);

                    // 使用join()等待全部任务完成
                    userFuture.join();
                    categoryFuture.join();
//                    long end = System.currentTimeMillis();
//                    System.out.println("================ 结束查询 " + video.getVid() + " 号视频相关信息 ===============   当前时间 " + end + "   耗时 " + (end - start));

                    return map;
                })
                .collect(Collectors.toList());

//        end = System.currentTimeMillis();
//        System.out.println("封装耗时：" + (end - start));
        return mapList;
        return customResponse;
    }


    /**
     *  在历史记录中新增条目
     *  注意：当文章已经出现在记录中时，需要更新这个条目
     */
    public CustomResponse insertInHistory(Integer pid){
        CustomResponse customResponse = new CustomResponse();

        return customResponse
    }

    /**
     *  在历史记录中删除条目
     * */
   public CustomResponse deleteInHistory(){
       CustomResponse customResponse = new CustomResponse();

       return customResponse;
   }


}
