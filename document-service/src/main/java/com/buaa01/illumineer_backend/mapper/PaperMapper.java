package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa01.illumineer_backend.entity.Paper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface PaperMapper extends BaseMapper<Paper> {

    // 获取指定文献ID的详细信息
    @Select("select * from paper where pid = #{pid} and stats = 0")
    Map<String, Object> getPaperByPid(Long pid);

    // 获取指定文献ID的详细信息
    @Select("select * from paper where stats = #{stats}")
    List<Map<String, Object>> getPapersByStats(int stats);

    // 获取检索结果
    @Select("SELECT * FROM paper WHERE MATCH(${condition}) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) AND stats = 0")
    List<Map<String, Object>> searchByKeywordWithFullText(String condition, String keyword);

    // 获取检索结果
    @Select("SELECT * FROM paper WHERE MATCH(${condition}) AGAINST(CONCAT(#{keyword}, '*') IN BOOLEAN MODE) AND stats = 0")
    List<Map<String, Object>> searchByKeywordWithBooleanMode(String condition, String keyword);

    // 获取检索结果
    @Select("SELECT * FROM paper WHERE MATCH(${condition}) AGAINST(#{keyword} IN BOOLEAN MODE) AND stats = 0")
    List<Map<String, Object>> searchByKeywordWithStrictBooleanMode(String condition, String keyword);

    // 获取检索结果
    @Select("SELECT * FROM paper WHERE ${condition} LIKE CONCAT(#{keyword}, '%') AND stats = 0")
    List<Map<String, Object>> searchByKeyword(String condition, String keyword);

    // 获取所有文献
    @Select("select * from paper where stats = 0")
    List<Map<String, Object>> getPapers();

    @Insert("insert into paper(pid,title,keywords,content_url,auths,category,type,theme,publish_date,derivation,ref_times,fav_times,refs,stats,ess_abs) values(#{pid},#{title},#{keywords},#{contentUrl},#{auths},#{field},#{type},#{theme},#{publishDate},#{derivation},#{refTimes},#{favTimes},#{refs},#{stats},#{essabs})")
    void insertPaper(Long pid,
                     String title,
                     String essabs,
                     String keywords,
                     String contentUrl,
                     String auths,
                     String field,
                     String type,
                     String theme,
                     Date publishDate,
                     String derivation,
                     String refs,
                     Integer refTimes,
                     Integer favTimes,
                     Integer stats);

    // 更新文献信息
    @Update("UPDATE paper " +
            "SET title = #{paper.title}, " +
            "auths = #{paper.auths} " +
            "WHERE pid = #{pid}")
    int updatePaper(Long pid, Map<String,Object> paper);

    // 推荐
    @Select("SELECT pid FROM paper " +
            "ORDER BY RAND() " +
            "LIMIT ${num}")
    List<Long> getRecommend(Integer num);

}
