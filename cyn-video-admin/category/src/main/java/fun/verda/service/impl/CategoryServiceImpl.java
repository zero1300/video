package fun.verda.service.impl;

import fun.verda.dao.CategoryDao;
import fun.verda.entity.Category;
import fun.verda.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("categoryService")
@Transactional
public class CategoryServiceImpl implements CategoryService {
    @Resource
    private CategoryDao categoryDao;
    @Override
    public List<Category> queryByFirstLevel() {
        return categoryDao.queryByFirstLevel();
    }

    @Override
    public Category update(Category category) {
        int row = categoryDao.update(category);
        return categoryDao.queryById(category.getId());
    }

    @Override
    public Category insert(Category category) {
        Date date = new Date();
        category.setCreatedAt(date);
        category.setUpdatedAt(date);
        int row = categoryDao.insert(category);
        return category;
    }

    @Override
    public void deleteById(Integer id) {
        categoryDao.deleteById(id);
    }
}
