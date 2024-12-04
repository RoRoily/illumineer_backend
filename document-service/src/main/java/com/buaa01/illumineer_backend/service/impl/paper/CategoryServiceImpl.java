package com.buaa01.illumineer_backend.service.impl.paper;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.mapper.CategoryMapper;
import com.buaa01.illumineer_backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Category getCategoryByID(String scid, String cid) {
        return categoryMapper.getCategoryBy2ID(scid, cid);
    }

    @Override
    public Category insertCategory(String scid, String cid, String sname, String name) {
        categoryMapper.insertCategory(scid, cid, sname, name);
        Category category = new Category();
        category = getCategoryByID(scid, cid);
        return category;
    }
}
