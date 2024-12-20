package com.buaa01.illumineer_backend.service.impl.history;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.DTO.HistoryDTO;
import com.buaa01.illumineer_backend.entity.History;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.PaperAdo;
import com.buaa01.illumineer_backend.mapper.HistoryMapper;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.service.client.PaperServiceClient;
import com.buaa01.illumineer_backend.service.history.HistoryService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class HistoryServiceImpl implements HistoryService {

    @Autowired
    private HistoryMapper historyMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTool redisTool;
    @Autowired
    private PaperServiceClient paperServiceClient;
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 分页返回历史记录中的条目
     * 并行处理
     *
     */
    @Override
    public CustomResponse getHistoryByPage(Integer uid, Integer quantity, Integer index){
        //从redis中获取历史记录条目集合
        String hisKey = "uForHis" + uid;
        Set<Object> pidList= redisTool.getSetMembers(hisKey);

        if (index == null) {
            index = 1;
        }
        if (quantity == null) {
            quantity = 10;
        }
        int startIndex = (index - 1) * quantity;
        int endIndex = startIndex + quantity;
        // 检查数据是否足够满足分页查询
        if (startIndex > pidList.size()) {
            // 如果数据不足以填充当前分页，返回空列表
            return new CustomResponse(200,"已查询结束",null);
        }
        // 使用线程安全的集合类 CopyOnWriteArrayList 保证多线程处理共享List不会出现并发问题
        List<Paper> paperList = new CopyOnWriteArrayList<>();

        // 直接数据库分页查询    （平均耗时 13ms）
        List<Object> idList = new ArrayList<>(pidList);
        endIndex = Math.min(endIndex, idList.size());
        //history的查询集合
        List<Object> sublist = idList.subList(startIndex, endIndex);
        List<PaperAdo> historyPaperList = paperServiceClient.getPaperAdoByList(sublist);

        // 并行处理每一个history条目，提高效率
        // 先将videoList转换为Stream
        Stream<PaperAdo> paperStream = historyPaperList.stream();
        List<Map<String, Object>> mapList = paperStream.parallel() // 利用parallel()并行处理
                .map(paper -> {
//                    long start = System.currentTimeMillis();
//                    System.out.println("================ 开始查询 " + video.getVid() + " 号视频相关信息 ===============   当前时间 " + start);
                    Map<String, Object> map = new HashMap<>();
                    map.put("paperTitle", paper.getTitle());

                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        StringBuilder Auths= new StringBuilder();
                        for(Map.Entry<String,Integer>entry:paper.getAuths().entrySet()){
                            Auths.append(entry.getKey()).append(",");
                        }
                        Auths.deleteCharAt(Auths.length()-1);
                        map.put("auth", Auths);
                    }, taskExecutor);

                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("publishDate",paper.getPublishDate());
                    }, taskExecutor);

                    // 使用join()等待全部任务完成
                    userFuture.join();
                    categoryFuture.join();
//                    long end = System.currentTimeMillis();
//                    System.out.println("================ 结束查询 " + video.getVid() + " 号视频相关信息 ===============   当前时间 " + end + "   耗时 " + (end - start));

                    return map;
                })
                .collect(Collectors.toList());
        CustomResponse customResponse = new CustomResponse();
        customResponse.setMessage("分页查询结果成功");
        customResponse.setData(mapList);
        customResponse.setCode(200);
//        end = System.currentTimeMillis();
//        System.out.println("封装耗时：" + (end - start));
        return customResponse;
    }

    /**
     *  在历史记录中新增条目
     *  注意：当文章已经出现在记录中时，需要更新这个条目
     */
    public CustomResponse insertInHistory(Integer pid){
        CustomResponse customResponse = new CustomResponse();

        return customResponse;
    }

    /**
     *  在历史记录中删除条目
     * */
   public CustomResponse deleteInHistory(){
       CustomResponse customResponse = new CustomResponse();

       return customResponse;
   }
}
