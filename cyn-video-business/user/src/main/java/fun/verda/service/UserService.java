package fun.verda.service;

import fun.verda.entity.User;
import org.springframework.stereotype.Service;

public interface UserService {
    User findByPhone(String phone);

    User insert(User user);

    void update(User user);

    User queryById(Integer valueOf);
}
