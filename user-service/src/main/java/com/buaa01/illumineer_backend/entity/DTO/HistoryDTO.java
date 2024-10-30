package com.buaa01.illumineer_backend.entity.DTO;

import java.util.Date;

/**
 * 历史记录条目
 * */
public class HistoryDTO {
    private Integer hid;//条目对应的历史记录下的hid
    private String paperName;//文献名称
    //    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date createDate;//存储时间
}
