package fun.verda.dao;


import fun.verda.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoDao {
    void insert(Video video);

    List<Video> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);

    List<Video> findAllByCategoryId(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("categoryId") Integer categoryId);

    Video queryById(Integer valueOf);
}
