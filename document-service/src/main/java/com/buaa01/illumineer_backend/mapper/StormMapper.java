package com.buaa01.illumineer_backend.mapper;

import com.buaa01.illumineer_backend.entity.Paper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.relational.core.sql.In;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface StormMapper {
    @Insert("insert into paper(pid,title,keywords,content_url,auths,field,type,theme,publish_date,derivation,ref_times,fav_times,refs,stats,ess_abs) values(#{pid},#{title},#{keywords},#{contentUrl},#{auths},#{field},#{type},#{theme},#{publishDate},#{derivation},#{refTimes},#{favTimes},#{refs},#{stats},#{essAbs})")
    void insertPaper(Long pid,
                     String title,
                     String essAbs,
                     List<String> keywords,
                     String contentUrl,
                     Map<String, Integer> auths,
                     String field,
                     String type,
                     String theme,
                     Date publishDate,
                     String derivation,
                     List<Long> refs,
                     Integer refTimes,
                     Integer favTimes,
                     Integer stats);
}