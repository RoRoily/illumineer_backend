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
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        long lastId = 0;
        try{
            while (true) {
                int offset = (pageNum - 1) * PAGE_SIZE;
                QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
                queryWrapper.gt("pid", lastId).orderByAsc("pid").last("LIMIT " + PAGE_SIZE);

                List<Paper> papers = paperMapper.selectList(queryWrapper);
                // 如果没有数据了，跳出循环
                if (papers.isEmpty()) {
                    break;
                }
                // 更新 lastId 为当前批次的最后一条数据 ID
                lastId = papers.get(papers.size() - 1).getPid();
                // 并行处理当前页数据
                List<? extends Future<?>> futures = papers.stream()
                        .map(paper -> executorService.submit(() -> {
                            try {
                                if(!elasticSearchTool.isExistPaper(paper.getPid())){
                                    elasticSearchTool.addPaper(paper);
                                }
                            } catch (Exception e) {
                                // 单条数据处理异常，记录日志或处理
                                e.printStackTrace();
                            }
                        }))
                        .collect(Collectors.toList());

                // 等待所有任务完成
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        e.printStackTrace(); // 处理线程执行异常
                    }
                }
                // 处理完一批数据后，继续查询下一批
                pageNum++;
            }
        }finally {
            executorService.shutdown();
        }
        return new CustomResponse(200,"更新到ElasticSearch成功",null);
    }
}
