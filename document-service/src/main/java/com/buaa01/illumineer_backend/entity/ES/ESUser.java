package com.buaa01.illumineer_backend.entity.ES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESUser {
    private Integer uid;
    private String Username;
    private String Institution;
    private List<String> field;
}