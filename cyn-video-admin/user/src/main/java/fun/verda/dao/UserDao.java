package fun.verda.dao;

import fun.verda.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDao {
    List<User> findAllByKeywords(@Param("offset") int offset, @Param("limit") int limit, @Param("id") String id, @Param("name") String name, @Param("phone") String phone);

    Long findTotalCountByKeyWords(@Param("id") String id, @Param("name") String name, @Param("phone") String phone);
}
