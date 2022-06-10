package fun.verda.dao;

import fun.verda.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoDao {
    public Long findTotalCountByKeywords(@Param("id") String id, @Param("title") String name, @Param("categoryId") String categoryId, @Param("username") String username);

    List<Video> findAllByKeywords(@Param("offset") int offset, @Param("limit") int limit, @Param("id") String id, @Param("title") String name, @Param("categoryId") String categoryId, @Param("username") String username);
}
