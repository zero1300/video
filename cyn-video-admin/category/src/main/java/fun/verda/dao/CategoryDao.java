package fun.verda.dao;

import fun.verda.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CategoryDao {
    List<Category> queryByFirstLevel();

    int update(Category category);

    Category queryById(Integer parentId);

    int insert(Category category);

    void deleteById(Integer id);
}

