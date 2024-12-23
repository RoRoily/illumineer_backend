package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppealEntry {
    @TableId(type = IdType.AUTO)
    private Integer appealId;

    private Long pid;

    private Integer appellantId;

    private Integer ownerId;

    private boolean isAcceptedByAppellant;

    private boolean accomplish;

    private Date appealTime;
    private Date handleTime;

    public AppealEntry(Long pid,Integer aid,Integer ownerIdd){
        Date now = new Date();
        this.setPid(pid);
        this.setAppellantId(aid);
        this.setOwnerId(ownerIdd);
        this.setAcceptedByAppellant(false);
        this.setAccomplish(false);
        this.setAppealTime(now);
        this.setHandleTime(null);
    }
}
