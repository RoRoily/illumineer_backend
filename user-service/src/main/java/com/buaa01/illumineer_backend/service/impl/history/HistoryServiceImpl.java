package com.buaa01.illumineer_backend.service.impl.history;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.*;
import com.buaa01.illumineer_backend.entity.DTO.HistoryDTO;
import com.buaa01.illumineer_backend.mapper.HistoryMapper;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.service.client.PaperServiceClient;
import com.buaa01.illumineer_backend.service.history.HistoryService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.parameters.P;
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
     */
    @Override
    public CustomResponse getHistoryByPage(Integer uid) {
        System.out.println(uid);
        //从redis中获取历史记录条目集合
        String hisKey = "uForHis:" + uid;
        ObjectMapper objectMapper = new ObjectMapper();
        Set<Long> pidList = null;
        try {
            Set<Object> pidObj = redisTool.zRange(hisKey, 0, -1);
            pidList = objectMapper.readValue(pidObj.toString(), new TypeReference<Set<Long>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 直接数据库分页查询    （平均耗时 13ms）
        List<Long> idList = new LinkedList<>(pidList);
        Integer endIndex = Math.min(50, idList.size());
        //history的查询集合
        List<Long> sublist = idList.subList(0, endIndex);
        Integer total = idList.size();

        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("uid", uid);
        User user = userMapper.selectOne(queryWrapper);

        // 将 List<Long> 转换为逗号分隔的字符串
        String pids = sublist.stream()
                .map(String::valueOf) // 将每个 Long 转换为 String
                .collect(Collectors.joining(",")); // 使用逗号连接
        pids = user.getName() + ":" + pids;
        List<PaperAdo> historyPaperList = paperServiceClient.getPaperAdoByList(pids);
        List<PaperAdo> papers = new LinkedList<>();
        for (int i = historyPaperList.size() - 1; i >= 0; i--) {
            papers.add(historyPaperList.get(i));
        }

        // 并行处理每一个history条目，提高效率
        // 先将videoList转换为Stream
//        Stream<PaperAdo> paperStream = historyPaperList.stream();
//        List<Map<String, Object>> mapList = paperStream.parallel() // 利用parallel()并行处理
//                .map(paper -> {
////                    long start = System.currentTimeMillis();
////                    System.out.println("================ 开始查询 " + video.getVid() + " 号视频相关信息 ===============   当前时间 " + start);
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("paperTitle", paper.getTitle());

//                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
//                        StringBuilder Auths = new StringBuilder();
//                        for (Map.Entry<String, Integer> entry : paper.getAuths().entrySet()) {
//                            Auths.append(entry.getKey()).append(",");
//                        }
//                        Auths.deleteCharAt(Auths.length() - 1);
//                        map.put("auth", Auths);
//                    }, taskExecutor);

//                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
//                        map.put("publishDate", paper.getPublishDate());
//                    }, taskExecutor);

        // 使用join()等待全部任务完成
//                    userFuture.join();
//                    categoryFuture.join();
//                    long end = System.currentTimeMillis();
//                    System.out.println("================ 结束查询 " + video.getVid() + " 号视频相关信息 ===============   当前时间 " + end + "   耗时 " + (end - start));

//                    return map;
//                })
//                .collect(Collectors.toList());

        CustomResponse customResponse = new CustomResponse();
        customResponse.setMessage("分页查询结果成功");
        customResponse.setData(papers);
        customResponse.setCode(200);
//        end = System.currentTimeMillis();
//        System.out.println("封装耗时：" + (end - start));
        return customResponse;
    }

    /**
     * 在历史记录中新增条目
     * 注意：当文章已经出现在记录中时，需要更新这个条目
     */
    public CustomResponse insertInHistory(Integer userID, Long pid) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Integer hID = userID;
            String favKey = "uForHis:" + userID;

            redisTool.storeZSetByTime(favKey, pid);
            QueryWrapper<History> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("hid", hID);
            History history = historyMapper.selectOne(queryWrapper);
            if (history == null) {
                historyMapper.insert(new History(hID, userID, 0));
            } else {
                UpdateWrapper<History> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("hid", hID);
                updateWrapper.setSql("count = count + 1");
                historyMapper.update(null, updateWrapper);
            }
            customResponse.setMessage("添加历史记录成功");
            customResponse.setData(hID);
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("添加历史记录失败");
        }

        return customResponse;
    }

    /**
     * 在历史记录中删除条目
     */
    public CustomResponse deleteInHistory(Integer userId, List<Long> pids) {
        CustomResponse customResponse = new CustomResponse();
        String favKey = "uForHis:" + userId;
        for (Long pid : pids) {
            if (!redisTool.isExistInZSet(favKey, pid)) {
                customResponse.setCode(500);
                customResponse.setMessage("该历史记录不存在");
            } else {
                redisTool.deleteZSetMember(favKey, pid);
            }
            QueryWrapper<History> queryWrapper = new QueryWrapper<>();
            Integer hID = userId;
            queryWrapper.eq("hid", hID);
            History history = historyMapper.selectOne(queryWrapper);
            if (history == null) {
                historyMapper.insert(new History(hID, userId, 0));
            } else {
                UpdateWrapper<History> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("hid", hID);
                updateWrapper.setSql("count = count - 1");
                historyMapper.update(null, updateWrapper);
            }
        }
        return customResponse;
    }
}
