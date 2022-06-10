package fun.verda.service.impl;

import fun.verda.dao.CategoryDao;
import fun.verda.entity.Category;
import fun.verda.service.CategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("categoryService")
public class CategoryServiceImpl implements CategoryService {
    @Resource
    private CategoryDao categoryDao;

    @Override
    public List<Category> findAll() {
        return categoryDao.findAll();
    }

    @Override
    public Category queryById(Integer id) {
        return categoryDao.queryById(id);
    }
}
