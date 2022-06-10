package fun.verda.controller;

import fun.verda.entity.Category;
import fun.verda.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/categories")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    @GetMapping
    public List<Category> categories() {
        return categoryService.queryByFirstLevel();
    }

    @PatchMapping("/{id}")
    public Category update(@PathVariable("id") Integer id, @RequestBody Category category){
        //1.更新
        category.setId(id);
        return categoryService.update(category);
    }

    @PostMapping
    public Category save(@RequestBody Category category){
        return categoryService.insert(category);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") Integer id){
        categoryService.deleteById(id);
        return "删除成功";
    }
}

