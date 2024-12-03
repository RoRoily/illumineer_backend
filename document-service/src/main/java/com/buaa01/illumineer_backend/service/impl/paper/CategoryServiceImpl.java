package com.buaa01.illumineer_backend.service.impl.user;

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
    public Category getCategoryByName(String name) throws SQLException {
        return categoryMapper.getCategoryByName(name);
    }

    @Override
    public Category insertCategory(String name) throws SQLException {
        categoryMapper.insertCategory(name);
        Category category = new Category();
        category = getCategoryByName(name);
        return category;
    }
}
