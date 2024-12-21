package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Favorite;
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

    /**
     * 创建一收藏夹
     *
     * @return 返回一新的收藏夹id
     */
    @Override
    public CustomResponse createFav(String favName) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Integer userID = currentUser.getUserId();
            Integer fID = fidnumInstance.addFidnum();
            String favKey = "uForFav:" + userID;
            if (favName == null) {
                favName = "收藏夹" + fID;
            }

            redisTool.storeZSetByTime(favKey, fID);
            favoriteMapper.insert(new Favorite(fID, userID, 1, favName, 0, 0));
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
    public CustomResponse deleteFav(Integer fid) {
        CustomResponse customResponse = new CustomResponse();

        String favKey = "uForFav:" + currentUser.getUserId();
        if (!redisTool.isExistInZSet(favKey, fid)) {
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

        String favKey = "fid:" + fid;
        if (!redisTool.isExist(favKey)) {
            customResponse.setCode(500);
            customResponse.setMessage("该收藏夹不存在");
        } else {
            favoriteMapper.selectById(fid).setTitle(newName);
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
        List<List<Integer>> pidslist = new ArrayList<>();
        try {
            String favKey = "uForFav:" + currentUser.getUserId();
            Set<Object> fids = redisTool.zRange(favKey, 0, -1);

            for (Object fid_Object : fids) {
                Integer fid = (Integer) fid_Object;
                String fidKey = "fid:" + fid;
                Set<Object> pids_Set = redisTool.zRange(fidKey, 0, -1);
                List<Integer> pids_List = pids_Set.stream()
                        .filter(obj -> obj instanceof Integer)
                        .map(obj -> (Integer) obj)
                        .collect(Collectors.toCollection(ArrayList::new));

                paperNumsofFav.add(pids_List.size());
                favNames.add(favoriteMapper.selectById(fid).getTitle());
                pidslist.add(pids_List);
            }

            data.put("pids", pidslist);
            data.put("favNames", favNames);
            data.put("paperNumsofFav", paperNumsofFav);
            data.put("fids", fids);
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
            List<String> favNames = new ArrayList<>();

            for (Integer fid : userFids) {
                String fidKey = "fid:" + fid;
                if (!redisTool.isExistInZSet(fidKey, pid)) {
                    retFids.add(fid);
                }
                favNames.add(favoriteMapper.selectById(fid).getTitle());
            }

            favsInfos.put("All fids", userFids);
            favsInfos.put("All fav name", favNames);

            data.put("All favs user have", favsInfos);
            data.put("All favs which have pid in user's favs", retFids);

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
