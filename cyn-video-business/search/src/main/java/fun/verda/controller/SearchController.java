package fun.verda.controller;

import fun.verda.service.VideoSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
public class SearchController {

    @Resource
    private VideoSearchService videoSearchService;

    @GetMapping("/search/videos")
    public Map<String,Object> videos(@RequestParam(value = "q") String q, @RequestParam(value = "page",defaultValue = "1") Integer page, @RequestParam(value = "per_page",defaultValue = "5") Integer rows){
        return videoSearchService.videos(q,page,rows);
    }
}
