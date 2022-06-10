package fun.verda.controller;

import fun.verda.entity.Video;
import fun.verda.service.VideoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/videos")
public class VideoController {
    @Resource
    private VideoService videoService;
    @GetMapping
    public Map<String, Object> videos(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                      @RequestParam(value = "per_page", defaultValue = "5") Integer rows,
                                      String id, // 视频id
                                      String name,  // 视频名称
                                      @RequestParam(value = "category_id", required = false) String categoryId,
                                      @RequestParam(value = "uploader_name", required = false) String username
                                      ){
        Map<String, Object> result = new HashMap<>();
        List<Video> items = videoService.findAllByKeywords(page, rows, id, name, categoryId, username);
        Long total = videoService.findTotalCountByKeywords(id, name, categoryId, username);
        result.put("total_count", total);
        result.put("items", items);
        return result;
    }
}
