package com.buaa01.illumineer_backend.mapper;

import com.buaa01.illumineer_backend.entity.Paper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StormMapper {
    @Insert("insert into paper(pid,title,keywords,content_url,auths,field,type,theme,publish_date,derivation,ref_times,fav_times,refs,stats,ess_abs) values(#{pid},#{title},#{keywords},#{contentUrl},#{auths},#{field},#{type},#{theme},#{publishDate},#{derivation},#{refTimes},#{favTimes},#{refs},#{stats},#{essAbs})")
    void insertPaper(Paper article);
}