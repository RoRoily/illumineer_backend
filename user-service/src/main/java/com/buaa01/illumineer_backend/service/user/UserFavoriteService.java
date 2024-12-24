package com.buaa01.illumineer_backend.service.user;

import java.util.List;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import org.springframework.data.relational.core.sql.In;

public interface UserFavoriteService {
    /**
     * 创建一个收藏夹
     *
     * @param favName
     * @return
     */
    public CustomResponse createFav(String favName, Integer userID);

    /**
     * 删除对应收藏夹
     *
     * @param fid
     * @return
     */
    public CustomResponse deleteFav(Integer fid, Integer userID);

    /**
     * 修改收藏夹名称
     *
     * @param fid
     * @param newName
     * @return
     */
    public CustomResponse changeFavName(Integer fid, String newName);

    /**
     * 在收藏夹中添加文章
     *
     * @param fid
     * @param pid
     * @return
     */
    public CustomResponse addPapertoFav(Integer fid, Long pid);

    /**
     * 在收藏夹中移除文章
     *
     * @param pid
     * @param fid
     * @return
     */
    public CustomResponse removePaperfromFav(Integer fid, Long pid);

    /**
     * 查找用户的所有收藏夹,返回所有收藏夹名、收藏夹id、收藏夹内文件夹数量于
     **/
    public CustomResponse searchAll(Integer uid);

    /**
     * 用以在文献页面直接对该文献进行批量收藏取消收藏操作
     *
     * @param pid  文献id
     * @param fids bilibili式框选，若选中则返回对应收藏夹id，若未选中则不返回
     * @return
     */
    public CustomResponse ProcessFavBatch(Long pid, List<Integer> fids);

    /**
     * 查看单文献在所有收藏夹的fid
     *
     * @param pid
     * @return CustomResponse
     **/
    public CustomResponse ReturnPidsinAllUserFavs(Long pid);

    /**
     * 查找用户收藏夹内所有文献
     *
     * @param fid 收藏夹id
     * @return CustomResponse
     */

    CustomResponse getPapersByFid(Integer fid);
}
