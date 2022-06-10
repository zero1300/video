package fun.verda.service;

import fun.verda.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> findAll();

    Category queryById(Integer id);
}
