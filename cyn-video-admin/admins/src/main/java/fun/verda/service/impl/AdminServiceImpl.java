package fun.verda.service.impl;

import com.alibaba.druid.util.StringUtils;
import fun.verda.dao.AdminDao;
import fun.verda.entity.Admin;
import fun.verda.service.AdminService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;


@Service("adminService")
@Transactional
public class AdminServiceImpl implements AdminService {

    @Resource
    private AdminDao adminDao;

    @Override
    public Admin login(Admin admin) {
        Admin adminDB = adminDao.findByUserName(admin.getUsername());
        // 判断用户是否存在
        if (ObjectUtils.isEmpty(adminDB)) throw new RuntimeException("用户名错误");
        // 判断密码
        String password = DigestUtils.md5DigestAsHex(admin.getPassword().getBytes(StandardCharsets.UTF_8));
        if(!StringUtils.equals(password,adminDB.getPassword())){throw new RuntimeException("密码输入错误！");}
        return adminDB;
    }
}
