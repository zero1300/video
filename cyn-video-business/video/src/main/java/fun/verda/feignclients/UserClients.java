package fun.verda.feignclients;

import fun.verda.entity.Comment;
import fun.verda.entity.Favorite;
import fun.verda.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient("API-USERS")
public interface UserClients {

    @GetMapping("/userInfo/{id}")
    User user(@PathVariable("id") String id);

    // 判断用户是否收藏了视频
    @GetMapping("/userInfo/favorite")
    Favorite favorite(@RequestParam("videoId") String videoId, @RequestParam("userId") String userId);

    //用户评论
    @PostMapping("/user/comment/{userId}/{videoId}")
    void comments(@PathVariable("userId") Integer userId,@PathVariable("videoId") Integer videoId, @RequestBody Comment comment);


    //评论列表
    @GetMapping("/user/comments")
    public Map<String,Object> comments(@RequestParam("videoId") Integer videoId,
                                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                                       @RequestParam(value = "per_page",defaultValue = "15") Integer rows);
}
