package com.buaa01.illumineer_backend.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
public class Institution {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String domain;
    private String name;

    public Institution() {}

    public Institution(String domain, String name) {
        this.domain = domain;
        this.name = name;
    }

    // Getters and setters
}
