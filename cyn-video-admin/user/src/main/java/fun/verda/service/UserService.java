package fun.verda.service;

import fun.verda.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService  {
    public List<User> findAllByKeywords(Integer pageNow, Integer rows, String id, String name, String phone);

    public Long findTotalCountByKeyWords(String id, String name, String phone);
}
