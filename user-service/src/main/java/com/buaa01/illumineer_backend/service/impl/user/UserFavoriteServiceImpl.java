package com.buaa01.illumineer_backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Favorite;
import com.buaa01.illumineer_backend.entity.History;
import com.buaa01.illumineer_backend.entity.singleton.FidnumSingleton;
import com.buaa01.illumineer_backend.mapper.FavoriteMapper;
import com.buaa01.illumineer_backend.service.client.PaperServiceClient;
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
import java.util.stream.Collectors;

@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {

    FidnumSingleton fidnumInstance = FidnumSingleton.getInstance();

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RedisTool redisTool;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private PaperServiceClient paperServiceClient;

    @Autowired
    private UserServiceImpl userServiceImpl;

    /**
     * 创建一收藏夹
     *
     * @return 返回一新的收藏夹id
     */
    @Override
    public CustomResponse createFav(String favName, Integer userID) {
        CustomResponse customResponse = new CustomResponse();

        try {
            String favKey = "uForFav:" + userID;
            Integer fidBias = currentUser.getUser().getFavBias();
            Integer fID = (int) ((fidBias + 1) * 10000 + userID);
            if (favName == null) {
                favName = "收藏夹" + fID;
            }

            redisTool.storeZSetByTime(favKey, fID);
            favoriteMapper.insert(new Favorite(fID, userID, 2, favName, 0, 0));
            userServiceImpl.updataUserFavBias();
            customResponse.setMessage("新建收藏夹成功");
            customResponse.setData(fID);
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("新建收藏夹失败");
        }

        return customResponse;
    }

    /**
     * 删除一个收藏夹
     */
    @Override
    public CustomResponse deleteFav(Integer fid, Integer userID) {
        CustomResponse customResponse = new CustomResponse();

        String favKey = "uForFav:" + userID;
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fid", fid);
        Favorite favorite = favoriteMapper.selectOne(queryWrapper);
        if (favorite == null) {
            customResponse.setCode(500);
            customResponse.setMessage("该收藏夹不存在");
        } else {
            redisTool.deleteZSetMember(favKey, fid);
            favoriteMapper.deleteById(fid);
        }
        return customResponse;
    }

    /**
     * 收藏夹重命名
     */
    @Override
    public CustomResponse changeFavName(Integer fid, String newName) {
        CustomResponse customResponse = new CustomResponse();

        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fid", fid);
        Favorite favorite = favoriteMapper.selectOne(queryWrapper);
        if (favorite == null) {
            customResponse.setCode(500);
            customResponse.setMessage("该收藏夹不存在");
        } else {
            UpdateWrapper<Favorite> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("fid", fid);
            updateWrapper.set("title", newName);
            favoriteMapper.update(null, updateWrapper);
            customResponse.setMessage("收藏夹更名成功！");
        }
        return customResponse;
    }

    /**
     * 收藏一篇文章
     *
     * @param pid 文章id
     * @param fid 收藏夹id
     */
    @Override
    public CustomResponse addPapertoFav(Integer fid, Long pid) {
        CustomResponse customResponse = new CustomResponse();
        // fid不存在
        String fidKey = "fid:" + fid;
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fid", fid);
        Favorite favorite = favoriteMapper.selectOne(queryWrapper);
        if (favorite == null) {
            customResponse.setCode(500);
            customResponse.setMessage("该收藏夹不存在");
        }
        // fid下已经有该论文
        else if (redisTool.isExistInZSet(fidKey, pid)) {
            customResponse.setCode(500);
            customResponse.setMessage("收藏夹" + fid + "已存在该论文");
        }
        // 收藏论文
        else {
            redisTool.storeZSetByTime(fidKey, pid);
            UpdateWrapper<Favorite> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("fid", fid);
            updateWrapper.setSql("count = count + 1");
            favoriteMapper.update(null, updateWrapper);
            customResponse.setCode(200);
            customResponse.setMessage("收藏成功");
        }
        return customResponse;
    }

    /**
     * 在收藏夹中移除文章
     **/
    @Override
    public CustomResponse removePaperfromFav(Integer fid, Long pid) {
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
            UpdateWrapper<Favorite> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("fid", fid);
            updateWrapper.setSql("count = count - 1");
            favoriteMapper.update(null, updateWrapper);
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
    public CustomResponse searchAll(Integer uid) {
        CustomResponse customResponse = new CustomResponse();
        List<Map<String, Object>> data = new ArrayList<>();
        try {
            QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", uid);
            List<Favorite> favorites = favoriteMapper.selectList(queryWrapper);

            for (Favorite favorite : favorites) {
                Integer fid = favorite.getFid();
                String fidKey = "fid:" + fid;
                Set<Object> pids_Set = redisTool.zRange(fidKey, 0, -1);
                List<Number> pids_List = pids_Set.stream()
                        .filter(obj -> obj instanceof Integer || obj instanceof Long)
                        .map(obj -> (Number) obj)
                        .collect(Collectors.toList());

                Map<String, Object> fav = new HashMap<>();
                fav.put("fid", fid);
                fav.put("favName", favorite.getTitle());
                fav.put("num", pids_List.size());

                // Map<Integer, String> pidMap = new HashMap<>();
                // for (Long pid : pids_List) {
                // pidMap.put(pid, paperServiceClient.getPaperByPid(pid).getData());
                // }
                // fav.put("pidInfo", pidMap);
                fav.put("pidList", pids_List);

                data.add(fav);
            }
            customResponse.setData(data);
            customResponse.setMessage("返回所有收藏夹成功");
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("返回所有收藏夹时程出现错误！");
        }
        return customResponse;
    }

    /**
     * 对单文献进行批量收藏
     * 
     * @param pid  文献id
     * @param fids 收藏夹id 选中的是需要进行收藏的
     * 
     */
    @Override
    public CustomResponse ProcessFavBatch(Long pid, List<Integer> fids) {
        CustomResponse customResponse = new CustomResponse();

        try {
            List<Integer> userFids = getUserFids();
            List<Integer> needDelete = new ArrayList<>(userFids);
            needDelete.removeAll(fids);

            for (Integer fid : needDelete) { // remove
                removePaperfromFav(fid, pid);
            }

            for (Integer fid : fids) { // add
                addPapertoFav(fid, pid);
            }
            customResponse.setMessage("对单文献批量操作收藏夹成功");
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("对单文献批量操作收藏夹时出现错误！");
        }

        return customResponse;
    }

    /**
     * 查看单文献在所有收藏夹的fid
     * 
     * @param pid
     * @return
     */
    @Override
    public CustomResponse ReturnPidsinAllUserFavs(Long pid) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> data = new HashMap<>();

            // 用户所有收藏夹fid
            List<Integer> userFids = getUserFids();

            // 收藏夹信息
            Map<String, Object> favsInfos = new HashMap<>();
            List<Integer> retFids = new ArrayList<>();

            for (Integer fid : userFids) {
                String fidKey = "fid:" + fid;
                if (redisTool.isExistInZSet(fidKey, pid)) {
                    retFids.add(fid);
                }
                favsInfos.put(fid.toString(), favoriteMapper.selectById(fid).getTitle());
            }

            data.put("pidInFavs", retFids);
            data.put("UserFavs", favsInfos);

            customResponse.setData(data);
            customResponse.setMessage("获取单文献在所有收藏夹的fid成功");
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("获取单文献在所有收藏夹的fid失败");
        }

        return customResponse;
    }

    /**
     * 获取用户所有收藏夹fid
     *
     * @return 返回List<Integer>方便操作
     * @throws Exception
     */
    private List<Integer> getUserFids() throws Exception {
        String favKey = "uForFav:" + currentUser.getUserId();
        Set<Object> fids_Set = redisTool.zRange(favKey, 0, -1);
        // System.err.println(fids_Set);
        List<Integer> fids_List = fids_Set.stream()
                .filter(obj -> obj instanceof Integer)
                .map(obj -> (Integer) obj)
                .collect(Collectors.toCollection(ArrayList::new));
        return fids_List;
    }

    /**
     * 查找用户收藏夹内所有文献
     *
     * @param fid 收藏夹id
     * @return CustomResponse
     */

    public CustomResponse getPapersByFid(Integer fid) {
        return paperServiceClient.getPaperByFid(fid);
    }

}
