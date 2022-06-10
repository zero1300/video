package fun.verda.dao;

import fun.verda.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminDao {
    Admin findByUserName(String username);
}

