package fun.verda.dao;

import fun.verda.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {
    User findByPhone(String phone);

    int insert(User user);

    void update(User user);

    User queryById(Integer id);
}
