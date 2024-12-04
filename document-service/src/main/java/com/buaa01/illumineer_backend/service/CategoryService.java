package com.buaa01.illumineer_backend.service;

import com.buaa01.illumineer_backend.entity.Category;

import java.sql.SQLException;

public interface CategoryService {
    Category getCategoryByID(String scid, String cid) throws SQLException;

    Category insertCategory(String  scid, String cid, String sname, String name) throws SQLException;
}
