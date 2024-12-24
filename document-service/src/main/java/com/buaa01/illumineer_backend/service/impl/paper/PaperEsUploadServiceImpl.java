package com.buaa01.illumineer_backend.service.impl.paper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperEsUploadService;
import com.buaa01.illumineer_backend.tool.ESTool;
import com.buaa01.illumineer_backend.tool.ElasticSearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PaperEsUploadServiceImpl implements PaperEsUploadService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private ElasticSearchTool elasticSearchTool;

    private static final int PAGE_SIZE = 1000; // 每次查询1000条数据
    @Override
    public CustomResponse UploadPaperInEs(){
        int pageNum = 1;

        while(true){// 创建分页对象
            Page<Paper> page = new Page<>(pageNum, PAGE_SIZE);

            // 查询当前页的数据
            IPage<Paper> paperPage = paperMapper.selectPage(page, new QueryWrapper<>());

            // 获取当前页的数据
            List<Paper> papers = paperPage.getRecords();

            // 如果没有数据了，跳出循环
            if (papers.isEmpty()) {
                break;
            }

            for(Paper paper : papers){
                elasticSearchTool.addPaper(paper);
            }
            System.out.println("Success update Batch: " + pageNum);
            // 处理完一批数据后，继续查询下一批
            pageNum++;
        }
        return new CustomResponse(200,"更新到ElasticSearch成功",null);
}

}
