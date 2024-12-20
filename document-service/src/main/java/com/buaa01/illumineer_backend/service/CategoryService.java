package com.buaa01.illumineer_backend.service;

import com.buaa01.illumineer_backend.entity.Category;

import java.sql.SQLException;
import java.util.List;

public interface CategoryService {
    Category getCategoryByID(String scid);

    Category insertCategory(String  scid, String cid, String sname, String name) throws SQLException;

    List<Category> getAllCategory();
}
