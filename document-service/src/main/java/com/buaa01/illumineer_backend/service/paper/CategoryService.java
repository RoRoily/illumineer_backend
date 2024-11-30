package com.buaa01.illumineer_backend.service.paper;

import com.buaa01.illumineer_backend.entity.Category;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public interface CategoryService {
    Category getCategoryByName(String name) throws SQLException;

    Category insertCategory(String name) throws SQLException;
}
