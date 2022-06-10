package fun.verda.controller;

import fun.verda.entity.User;
import fun.verda.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping
    public Map<String, Object> users(@RequestParam(value = "page", defaultValue = "1") Integer pageNow,
                                     @RequestParam(value = "pre_page", defaultValue = "5") Integer rows,
                                     @RequestParam(required = false) String id,
                                     String name,
                                     String phone
                                     ){
        Map<String, Object> result = new HashMap<>();
        List<User> items = userService.findAllByKeywords(pageNow, rows, id, name, phone);

        Long total = userService.findTotalCountByKeyWords(id, name, phone);

        result.put("total_count", total);
        result.put("items", items);
        return result;

    }
}
