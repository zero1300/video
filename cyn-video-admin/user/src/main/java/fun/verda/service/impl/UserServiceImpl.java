package fun.verda.service.impl;

import fun.verda.dao.UserDao;
import fun.verda.entity.User;
import fun.verda.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;


    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<User> findAllByKeywords(Integer offset, Integer limit, String id, String name, String phone) {
        int start = (offset - 1) * limit;
        return userDao.findAllByKeywords(start, limit, id, name, phone);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Long findTotalCountByKeyWords(String id, String name, String phone) {
        return userDao.findTotalCountByKeyWords(id, name, phone);
    }
}
