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
    Integer appealId;

    Long pid;

    Integer appellant_id;

    Integer owner_id;

    boolean isAcceptedByAppellant;

    boolean accomplish;

    Date appealTime;
    Date handleTime;

    public AppealEntry(PaperAdo paperAdo,User appellant,User owner){
        Date now = new Date();
        this.setPid(paperAdo.getPid());
        this.setAppellant_id(appellant.getUid());
        this.setOwner_id(owner.getUid());
        this.setAcceptedByAppellant(false);
        this.setAccomplish(false);
        this.setAppealTime(now);
        this.setHandleTime(null);
    }
}
