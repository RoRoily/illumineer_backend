package com.buaa01.illumineer_backend.service.impl.paper;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaperStatsServiceImpl implements PaperStatsService {

    @Autowired
    private PaperMapper paperMapper;

    /**
     * @description: 更新文章状态（0 正常 1 已删除 2 待审核）
     *               <p>
     *               下架文献：stats = 1
     *               <p>
     *               刚上传文献 / 文献被投诉：stats = 2
     *               <p>
     *               文献审核完毕：stats = 0
     * @param: [pid, stats 要更新成什么状态]
     * @return: 是否成功更新文章状态
     **/
    @Override
    public CustomResponse updateStats(Long pid, Integer stats) {
        CustomResponse customResponse = new CustomResponse();
        UpdateWrapper<Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid);
        updateWrapper.setSql("stats = " + stats);

        paperMapper.update(null, updateWrapper);

        customResponse.setMessage("更新文章状态成功！");
        return customResponse;
    }
}
