package fun.verda.controller;


import fun.verda.dao.AdminDao;
import fun.verda.dto.AdminDTO;
import fun.verda.entity.Admin;
import fun.verda.service.AdminService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class AdminController {
    @Resource
    private AdminService adminService;

    @Resource
    private RedisTemplate<String, Admin> redisTemplate;

    @PostMapping("/tokens")
    public Map<String, String> tokens(@RequestBody Admin admin, HttpSession session) {
        Map<String, String> result = new HashMap<>();
        Admin adminDB = adminService.login(admin);
        String token = session.getId();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.opsForValue().set(token,adminDB,30, TimeUnit.MINUTES);
        result.put("token", token);
        return result;
    }

    @GetMapping("admin-user")
    public AdminDTO admin(String token) {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        Admin admin = (Admin) redisTemplate.opsForValue().get(token);
        AdminDTO adminDTO = new AdminDTO();
        assert admin != null;
        adminDTO.setName(admin.getUsername());
        adminDTO.setAvatar(admin.getAvatar());
        return adminDTO;
    }

    @DeleteMapping("/tokens/{token}")
    public void logout(@PathVariable("token") String token){
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.delete(token);

    }
}

