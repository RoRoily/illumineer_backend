package com.buaa01.illumineer_backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beust.ah.A;
import com.buaa01.illumineer_backend.entity.*;
import com.buaa01.illumineer_backend.mapper.AppealMapper;
import com.buaa01.illumineer_backend.service.client.PaperServiceClient;
import com.buaa01.illumineer_backend.service.user.AppealService;
import com.buaa01.illumineer_backend.service.user.UserService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AppealServiceImpl implements AppealService {



    @Autowired
    private PaperServiceClient paperServiceClient;

    @Autowired
    private UserService userService;

    @Autowired
    private AppealMapper appealMapper;

    @Autowired
    private RedisTool redisTool;


    @Override
    public CustomResponse createAppealEntry(Integer appellantUid, String sameName, Integer conflictPaperPid){
        CustomResponse customResponse = new CustomResponse();
        //Paper conflictPaper = paperServiceClient.getPaperById(conflictPaperPid);
        //PaperAdo conflictPaperInfo = new PaperAdo(conflictPaper,sameName);
       // User appliant = userService.getUserByUId(appellantUid);
        //Integer ownerUid = conflictPaper.getAuths().get(sameName);
        Long pid  = Long.valueOf(conflictPaperPid);
        Integer ownerUid = paperServiceClient.getAuthId(sameName,pid);


        //User owner = userService.getUserByUId(ownerUid);

        AppealEntry appealEntry = new AppealEntry(pid,appellantUid,ownerUid);
        QueryWrapper<AppealEntry> queryWrapper = new QueryWrapper<>();
        //保存至数据库
        appealMapper.insert(appealEntry);
        customResponse.setMessage("成功创建申诉条目");
        customResponse.setCode(200);
        return customResponse;
    }


    @Override
    public CustomResponse judgeAppeal(Integer appealEntryId,boolean acceptAppeal){
     CustomResponse customResponse = new CustomResponse() ;
     QueryWrapper<AppealEntry> queryWrapper = new QueryWrapper<>();
     queryWrapper.eq("appeal_id", appealEntryId);
     AppealEntry appealEntry = appealMapper.selectOne(queryWrapper);
     if(appealEntry == null){
        return new CustomResponse(500,"未查找到待处理的记录",null);
     }

     if(acceptAppeal){
         //申诉成功，修改文章的所有者信息
         appealEntry.setAcceptedByAppellant(true);
         Integer appellantId = appealEntry.getAppellantId();
         User appellant = userService.getUserByUId(appellantId);

         Integer ownerId = appealEntry.getOwnerId();


         Long conflictPid = appealEntry.getPid();
         //对mysql中Paper实体类修改文章的所有者信息
         System.out.println("change auth params: " + conflictPid + " " +
                 appellant.getName() + " " +
                 appellant.getUid());
         paperServiceClient.modifyAuth(conflictPid, appellant.getName(), appellant.getUid());


         //修改Redis信息
         // paperBelonged : 拥有该文章的作者的uid集合
         // property : 某个作者拥有文章的pid集合
         String appellantKey = "property:" + appellantId;
         String ownerKey = "property:" + ownerId;
         String authKey = "paperBelonged:" + conflictPid;

         redisTool.deleteSetMember(ownerKey,conflictPid);
         redisTool.addSetMember(appellantKey,conflictPid);

         redisTool.deleteSetMember(authKey,ownerKey);
         redisTool.addSetMember(authKey,appellantKey);


     }
     appealEntry.setAccomplish(true);
     appealEntry.setHandleTime(new Date());
     appealMapper.updateById(appealEntry);
     return new CustomResponse(200,"成功处理记录",null);
    }

    @Override
    public CustomResponse displayAppealEntry(Integer quantity,Integer index,boolean handled){
        QueryWrapper<AppealEntry> appealEntryQueryWrapper = new QueryWrapper<>();
        appealEntryQueryWrapper.eq("accomplish",handled);
        List<AppealEntry> appealEntries = appealMapper.selectList(appealEntryQueryWrapper);
        if (index == null) {
            index = 1;
        }
        if (quantity == null) {
            quantity = 10;
        }
        int startIndex = (index - 1) * quantity;
        int endIndex = startIndex + quantity;
        // 检查数据是否足够满足分页查询
        if (startIndex > appealEntries.size()) {
            // 如果数据不足以填充当前分页，返回空列表
            return new CustomResponse(200,"已查询结束",null);
        }
        // 使用线程安全的集合类 CopyOnWriteArrayList 保证多线程处理共享List不会出现并发问题
        List<Paper> paperList = new CopyOnWriteArrayList<>();

        // 直接数据库分页查询    （平均耗时 13ms）
        List<AppealEntry> idList = new ArrayList<>(appealEntries);
        endIndex = Math.min(endIndex, idList.size());
        //history的查询集合
        List<AppealEntry> sublist = idList.subList(startIndex, endIndex);

        CustomResponse customResponse = new CustomResponse();
        customResponse.setMessage("分页查询结果成功");
        customResponse.setData(sublist);
        customResponse.setCode(200);
//        end = System.currentTimeMillis();
//        System.out.println("封装耗时：" + (end - start));
        return customResponse;

    }
}
