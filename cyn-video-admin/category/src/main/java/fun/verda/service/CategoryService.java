package fun.verda.service;

import fun.verda.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> queryByFirstLevel();

    Category update(Category category);

    Category insert(Category category);

    void deleteById(Integer id);
}