package fun.verda.dao;

import fun.verda.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface CategoryDao {

    Category queryById(Integer id);

    List<Category> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);

    List<Category> queryAll(Category category);

    int insert(Category category);

    int deleteById(Integer id);

    List<Category> findAll();

}