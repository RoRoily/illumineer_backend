package com.buaa01.illumineer_backend.entity.ES;

import com.buaa01.illumineer_backend.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESUser {
    private Integer uid;
    private String Username;
    private String Institution;
    private List<String> field;
}