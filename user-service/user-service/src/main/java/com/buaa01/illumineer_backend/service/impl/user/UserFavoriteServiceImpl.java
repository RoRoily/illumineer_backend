package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.singleton.FidnumSingleton;
import com.buaa01.illumineer_backend.service.user.UserFavoriteService;
import com.buaa01.illumineer_backend.service.utils.CurrentUser;
import com.buaa01.illumineer_backend.tool.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {

    FidnumSingleton fidnumInstance = FidnumSingleton.getInstance();

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RedisTool redisTool;

    /**
     * 创建一个收藏夹
     * */
    @Override
    public CustomResponse createFav(){
        CustomResponse customResponse = new CustomResponse();
        String favKey =  "uForFav:" + currentUser.getUserId();
        redisTool.storeZSetByTime(favKey,fidnumInstance.addFidnum());
        customResponse.setCode(200);
        customResponse.setMessage("新建收藏夹成功");
        return customResponse;
    }

    /**在收藏夹中添加文章
     * **/
    @Override
    public CustomResponse updateFav(Integer pid,Integer fid){
        CustomResponse customResponse = new CustomResponse();
        //fid不存在
        String fidKey = "fid:" + fid;
        if(!redisTool.isExist(fidKey)){
            customResponse.setCode(500);
            customResponse.setMessage("该收藏夹不存在");

        }
        //fid下已经有该论文
        else if(redisTool.isExistInZSet(fidKey,pid)){
            customResponse.setCode(500);
            customResponse.setMessage("收藏夹fid" + fid + "已存在该论文");

        }
        //收藏论文
        else{
            redisTool.storeZSetByTime(fidKey,pid);
            customResponse.setCode(200);
            customResponse.setMessage("收藏成功");
        }
        return customResponse;
    }

    /**
     * 查找用户的所有文件夹
     * 默认为登录的用户
     * 返回的收藏夹data为一个set
     * **/
        @Override
        public CustomResponse searchAll(){
            CustomResponse customResponse = new CustomResponse();
            String favKey =  "uForFav:" + currentUser.getUserId();
            Set<Object> fids = redisTool.zRange(favKey,0,-1);
            customResponse.setMessage("返回所有收藏夹成功");
            customResponse.setCode(200);
            customResponse.setData(fids);
            return customResponse;
        }
}
