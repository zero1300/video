package fun.verda.controller;

import com.alibaba.druid.support.json.JSONUtils;
import fun.verda.entity.Category;
import fun.verda.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("categories")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    @GetMapping
    public List<Category> categories() {
        log.info("进入查询类别列表方法..");
        List<Category> categories = categoryService.findAll();
        log.info("查询当前一级类别列表总数为: {}", categories.size());
        return categories;
    }

    @GetMapping("{id}")
    public Category category(@PathVariable("id") Integer id) {
        log.info("接收到的类别id: {}", id);
        Category category = categoryService.queryById(id);
        return category;
    }



}
