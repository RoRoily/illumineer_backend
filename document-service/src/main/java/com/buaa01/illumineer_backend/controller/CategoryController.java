package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.service.paper.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/category/get")
    public Category getCategoryByName(@RequestParam String id, String sub_id) throws Exception {
        return categoryService.getCategoryByID(sub_id, id);
    }
}
