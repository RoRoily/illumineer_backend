package com.buaa01.illumineer_backend.service;

import com.buaa01.illumineer_backend.entity.Category;

import java.sql.SQLException;

public interface CategoryService {
    Category getCategoryByName(String name) throws SQLException;

    Category insertCategory(String name) throws SQLException;
}
