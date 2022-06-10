package fun.verda.service.impl;

import fun.verda.dao.UserDao;
import fun.verda.entity.User;
import fun.verda.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;
    @Override
    public User findByPhone(String phone) {
        return userDao.findByPhone(phone);
    }

    @Override
    public User insert(User user) {
        user.setName(user.getPhone());
        userDao.insert(user);
        return user;
    }

    @Override
    public void update(User user) {
        userDao.update(user);
    }

    @Override
    public User queryById(Integer id) {
        //user.setFollowingCount(followingDao.findAllFollowingByUserId(user.getId()).size());
        //user.setFollowersCount(followingDao.findAllByUserId(user.getId()).size());
        return this.userDao.queryById(id);
    }
}
