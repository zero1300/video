package fun.verda.controller;

import fun.verda.entity.Comment;
import fun.verda.entity.Favorite;
import fun.verda.entity.User;
import fun.verda.service.CommentService;
import fun.verda.service.FavoriteService;
import fun.verda.service.UserService;
import fun.verda.utils.JSONUtils;
import fun.verda.vo.CommentVO;
import fun.verda.vo.Reviewer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class UserServiceController {
    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;

    @Resource
    private FavoriteService favoriteService;

    // 通过用户id获取用户信息
    @GetMapping("userInfo/{id}")
    public User user(@PathVariable("id") String id) {
        User user = userService.queryById(Integer.valueOf(id));
        return user;
    }

    // 查询视频下面的评论
    @GetMapping("/user/comments")
    public Map<String, Object> comments(@RequestParam("videoId") Integer videoId,
                                        @RequestParam(value = "page", defaultValue = "1") Integer page,
                                        @RequestParam(value = "per_page", defaultValue = "15") Integer rows) {
        log.info("视频id: {}", videoId);
        log.info("当前页: {}", page);
        log.info("每页显示记录数: {}", rows);
        Map<String, Object> result = new HashMap<>();

        //1.根据视频id分页查询当前视频下评论内容
        Long total_counts = commentService.findByVideoIdCounts(videoId);
        result.put("total_count", total_counts);

        //2.根据视频id分页获取对应评论信息以及子评论信息
        List<CommentVO> commentVOList = new ArrayList<>();
        //3.根据视频id分页获取父评论信息
        List<Comment> comments = commentService.findByVideoId(videoId, page, rows);

        //4.遍历父评论信息
        comments.forEach(comment -> {
            //5.将评论信息转为commentVO
            CommentVO commentVO = new CommentVO();
            //6.属性拷贝  commentvo id content createAt
            BeanUtils.copyProperties(comment, commentVO);
            //7.获取评论作者信息
            Reviewer reviewer = new Reviewer();
            //8.根据评论用户id查询用户信息
            User user = userService.queryById(comment.getUid());
            //9.将用户信息 赋值 reviewer对象中  id name avtar
            BeanUtils.copyProperties(user, reviewer);
            //10.设置评论信息
            commentVO.setReviewer(reviewer);
            //11.设置子评论内容
            List<Comment> childComments = commentService.findByParentId(comment.getId());
            List<CommentVO> childCommentVOS = new ArrayList<>();
            childComments.forEach(commentChild -> {
                CommentVO commentChildVO = new CommentVO();
                BeanUtils.copyProperties(commentChild, commentChildVO);
                //12.设置评论用户信息
                User userChild = userService.queryById(commentChild.getUid());
                Reviewer reviewerChild = new Reviewer();
                BeanUtils.copyProperties(userChild, reviewerChild);
                commentChildVO.setReviewer(reviewerChild);
                childCommentVOS.add(commentChildVO);
            });
            //13.设置子评论
            commentVO.setSubComments(childCommentVOS);
            //14.放入list
            commentVOList.add(commentVO);
        });
        result.put("items", commentVOList);
        return result;
    }

    // 用户发表评论
    @PostMapping("/user/comment/{userId}/{videoId}")
    public void comments(@PathVariable("userId") Integer userId, @PathVariable("videoId") Integer videoId, @RequestBody Comment comment) {
        //接受到评论
        log.info("视频id: {}", videoId);
        log.info("评论信息: {}", JSONUtils.writeValueAsString(comment));
        log.info("评论用户信息: {}", userId);
        //设置评论用户信息
        comment.setUid(userId);
        //设置评论视频
        comment.setVideoId(videoId);
        commentService.insert(comment);
    }

    // 根据用户id和视频id查询是否收藏
    @GetMapping("/userInfo/favorite")
    public Favorite favorite(@RequestParam("videoId") String videoId, @RequestParam("userId") String userId) {
        log.info("接收到的视频id {}, 用户id: {}", videoId, userId);
        Favorite favorite = favoriteService.queryByVideoIdAndUserId(Integer.valueOf(videoId), Integer.valueOf(userId));
        log.info("当前返回的收藏对象是否为空: {}", ObjectUtils.isEmpty(favorite));
        return favorite;
    }
}
