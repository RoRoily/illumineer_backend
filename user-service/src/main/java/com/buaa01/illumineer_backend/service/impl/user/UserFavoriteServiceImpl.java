package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Favorite;
import com.buaa01.illumineer_backend.entity.singleton.FidnumSingleton;
import com.buaa01.illumineer_backend.mapper.FavoriteMapper;
import com.buaa01.illumineer_backend.service.user.UserFavoriteService;
import com.buaa01.illumineer_backend.service.utils.CurrentUser;
import com.buaa01.illumineer_backend.tool.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {

    FidnumSingleton fidnumInstance = FidnumSingleton.getInstance();

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RedisTool redisTool;

    @Autowired
    private FavoriteMapper favoriteMapper;

    /**
     * 创建一个收藏夹
     */
    @Override
    public CustomResponse createFav() {
        CustomResponse customResponse = new CustomResponse();
        Integer userID = currentUser.getUserId();
        Integer fID = fidnumInstance.addFidnum();
        String favKey = "uForFav:" + userID;

        redisTool.storeZSetByTime(favKey, fID);
        favoriteMapper.insert(new Favorite(fID, userID, 1, "默认收藏夹", 0, 0));

        customResponse.setCode(200);
        customResponse.setMessage("新建收藏夹成功");
        return customResponse;
    }

    /**
     * 在收藏夹中添加文章
     **/
    @Override
    public CustomResponse updateFav(Integer pid, Integer fid) {
        CustomResponse customResponse = new CustomResponse();
        // fid不存在
        String fidKey = "fid:" + fid;
        if (!redisTool.isExist(fidKey)) {
            customResponse.setCode(500);
            customResponse.setMessage("该收藏夹不存在");

        }
        // fid下已经有该论文
        else if (redisTool.isExistInZSet(fidKey, pid)) {
            customResponse.setCode(500);
            customResponse.setMessage("收藏夹fid" + fid + "已存在该论文");
        }
        // 收藏论文
        else {
            redisTool.storeZSetByTime(fidKey, pid);
            favoriteMapper.selectById(fid).updataFavCounts(1);
            customResponse.setCode(200);
            customResponse.setMessage("收藏成功");
        }
        return customResponse;
    }

    /**
     * 在收藏夹中移除文章
     **/
    @Override
    public CustomResponse updateFav_Remove(Integer pid, Integer fid) {
        CustomResponse customResponse = new CustomResponse();
        // fid不存在
        String fidKey = "fid:" + fid;
        if (!redisTool.isExist(fidKey)) {
            customResponse.setCode(500);
            customResponse.setMessage("该收藏夹不存在");

        }
        // fid下不存在该论文
        else if (!redisTool.isExistInZSet(fidKey, pid)) {
            customResponse.setCode(500);
            customResponse.setMessage("收藏夹fid" + fid + "不存在该论文");
        }
        // 移除论文
        else {
            redisTool.deleteZSetMember(fidKey, pid);
            favoriteMapper.selectById(fid).updataFavCounts(0);
            customResponse.setCode(200);
            customResponse.setMessage("移除收藏成功");
        }
        return customResponse;
    }

    /**
     * 查找用户的所有文件夹
     * 默认为登录的用户
     * 
     * @return 返回对应用户所有收藏夹的title，count字段以及其中pid的集合(set<Object>)
     **/
    @Override
    public CustomResponse searchAll() {
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> data = new HashMap<>();
        List<Integer> paperNumsofFav = new ArrayList<>();
        List<String> favNames = new ArrayList<>();

        try {
            String favKey = "uForFav:" + currentUser.getUserId();
            Set<Object> fids = redisTool.zRange(favKey, 0, -1);

            for (Object fid_Object : fids) {
                Integer fid = (Integer) fid_Object;
                String fidKey = "fid:" + fid;
                Set<Object> pids = redisTool.zRange(fidKey, 0, -1);
                paperNumsofFav.add(pids.size());
                favNames.add(favoriteMapper.selectById(fid).getTitle());
            }

            data.put("favNames", favNames);
            data.put("paperNumsofFav", paperNumsofFav);
            data.put("fids", fids);
            customResponse.setData(data);

            customResponse.setMessage("返回所有收藏夹成功");
            return customResponse;
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("返回所有收藏夹时程出现错误！");
            return customResponse;
        }
    }

}
