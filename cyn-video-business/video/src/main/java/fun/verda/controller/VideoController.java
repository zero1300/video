package fun.verda.controller;

import fun.verda.entity.Comment;
import fun.verda.entity.User;
import fun.verda.feignclients.UserClients;
import fun.verda.service.VideoService;
import fun.verda.utils.JSONUtils;
import fun.verda.vo.VideoDetailVO;
import fun.verda.vo.VideoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class VideoController {

    @Resource
    private UserClients userClients;

    @Resource
    private VideoService videoService;

    @Resource
    private RedisTemplate<String, User> redisTemplate;


    // 查看分类下视频列表
    @GetMapping("/videos")
    public List<VideoVO> videos(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                @RequestParam(value = "per_page", defaultValue = "5") Integer rows,
                                @RequestParam("category") Integer categoryId
                                ){
        List<VideoVO> videoVOS = videoService.findAllByCategoryId(page, rows, categoryId);
        return videoVOS;
    }

    // 获得视频下的评论
    @GetMapping("/videos/{videoId}/comments")
    public Map<String, Object> comments(@PathVariable("videoId") Integer videoId,
                                        @RequestParam("page") Integer page,
                                        @RequestParam("per_page") Integer rows, HttpServletRequest request
                                         ) {
        return userClients.comments(videoId, page, rows);

    }

    // 在视频下方发表评论
    @PostMapping("/videos/{videoId}/comments")
    public void comments(@PathVariable("videoId") Integer videoId, @RequestBody Comment comment, HttpServletRequest request) {
        // 获取token
        String token = request.getParameter("token");
        // 根据 tokenKey 获取用户信息
        String tokenKey = "session_" + token;
        User user = (User) redisTemplate.opsForValue().get(tokenKey);
        userClients.comments(user.getId(), videoId, comment);
    }

    // 获取视频信息
    @GetMapping("/videos/{id}")
    public VideoDetailVO video(@PathVariable("id") String videoId, String token) {
        log.info("当前接收到的videoId: {}", videoId);
        VideoDetailVO videoDetailVO = videoService.queryDetailById(videoId, token);
        log.info("查询到的视频详细对象信息: {}", JSONUtils.writeValueAsString(videoDetailVO));
        return videoDetailVO;
    }
}
