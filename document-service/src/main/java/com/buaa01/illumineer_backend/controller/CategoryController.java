package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/category/getById")
    public CustomResponse getCategoryByID(@RequestParam String sub_id) throws Exception {
        CustomResponse customResponse = new CustomResponse();
        Category category = categoryService.getCategoryByID(sub_id);
        if (category == null) {
            customResponse.setCode(400);
            customResponse.setMessage("Category not found");
            return customResponse;
        }
        customResponse.setCode(200);
        customResponse.setMessage("OK");
        customResponse.setData(category);
        return customResponse;
    }

    @GetMapping("/category/getAll")
    public CustomResponse getAllCategory(){
        CustomResponse customResponse = new CustomResponse();
        List<Category> list = categoryService.getAllCategory();
        customResponse.setCode(200);
        customResponse.setMessage("OK");
        customResponse.setData(list);
        return customResponse;
    }
}
