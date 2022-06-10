package fun.verda.controller;

import fun.verda.entity.Video;
import fun.verda.service.VideoService;
import fun.verda.vo.VideoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class VideoServiceController {
    @Resource
    private VideoService videoService;

    @PostMapping("publish")
    public Video publish(@RequestBody Video video) {
        return videoService.insert(video);
    }

    @GetMapping("recommends")
    public List<VideoVO> recommends(@RequestParam("page") Integer page, @RequestParam("per_page") Integer per_page) {
        List<VideoVO> videoVOS = videoService.queryAllByLimit(page, per_page);
        log.info("视频推荐列表数量: {}", videoVOS.size());
        return videoVOS;
    }

    /**
     * 根据视频id查询视频的方法
     */
    @GetMapping("getVideos")
    public List<VideoVO> getVideos(@RequestParam("ids") List<Integer> ids){
        List<VideoVO> videoVOS = new ArrayList<>();
        log.info("ids: {}",ids);
        ids.forEach(id->{
            VideoVO videoVO = videoService.queryById(id);
            videoVOS.add(videoVO);
        });
        return videoVOS;
    }
}
