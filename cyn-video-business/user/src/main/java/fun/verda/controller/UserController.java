package fun.verda.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fun.verda.annotations.RequiredToken;
import fun.verda.entity.Favorite;
import fun.verda.entity.Played;
import fun.verda.entity.User;
import fun.verda.entity.Video;
import fun.verda.feignClient.VideoClient;
import fun.verda.service.FavoriteService;
import fun.verda.service.PlayedService;
import fun.verda.service.UserService;
import fun.verda.utils.ImageUtils;
import fun.verda.utils.JSONUtils;
import fun.verda.utils.OSSUtils;
import fun.verda.utils.RedisPrefix;
import fun.verda.vo.MsgVO;
import fun.verda.vo.VideoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private PlayedService playedService;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate<String, User> redisTemplate;

    private ReentrantLock lock = new ReentrantLock();

    @Resource
    private VideoClient videoClient;

    // 点击登录按钮时发起请求, 需要传入两个参数: phone, captcha
    @PostMapping("/tokens")
    public Map<String, Object> tokens(@RequestBody MsgVO msgVO, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 拿到手机和验证码
        String phone = msgVO.getPhone();
        String captcha = msgVO.getCaptcha();
        log.info("手机号: {},验证码:{}", phone, captcha);

        // 根据手机号判断redis中是否还存在该手机号验证码,如果不存在说明验证码已经过期!
        String phoneKey = "phone_" + phone;
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(phoneKey))) {
            throw new RuntimeException("提示:验证码已经过期!");
        }

        // 根据手机号获取redis中验证码
        String redisCaptcha = stringRedisTemplate.opsForValue().get(phoneKey);

        // 比较用户输入的验证码和redis中验证码是否一致
        if (!StringUtils.equals(captcha, redisCaptcha)) {
            throw new RuntimeException("提示:验证码输入错误!");
        }

        //6.判断是否为首次登录  //判断手机号之前是否登录过
        User user = userService.findByPhone(phone);
        if (ObjectUtils.isEmpty(user)) {
            user = new User();//创建一个用户对象
            user.setCreatedAt(new Date());//设置创建时间
            user.setUpdatedAt(new Date());//设置更新时间
            user.setPhone(phone); //设置用户的手机号
            user.setIntro("");//设置简介为空
            //初始化默认头像
            user.setAvatar(ImageUtils.getPhoto());//随机初始化头像
            user.setPhoneLinked(1);//是否绑定手机
            user.setWechatLinked(0);//是否绑定微信
            user.setFollowersCount(0);//设置粉丝数
            user.setFollowingCount(0);//设置关注数
            user = userService.insert(user);//保存用户信息
        }
        String token = request.getSession().getId(); //根据请求 sessionid  手机号
        String tokenKey = "session_" + token; //session_xxxxx  session_132...
        redisTemplate.opsForValue().set(tokenKey, user, 7, TimeUnit.DAYS);
        log.info("生成token: {}", token);
        result.put("token", token);
        return result;
    }

    @GetMapping("/user")
    @RequiredToken
    public User user(HttpServletRequest request) throws JsonProcessingException {
        User user = (User) request.getAttribute("user");
        log.info("获取用户信息位: {}", new ObjectMapper().writeValueAsString(user));
        return user;
    }

    @DeleteMapping("tokens")
    public void logout(String token) {
        String tokenKey = "session_" + token;
        log.info("Begin delete token {} in redis", token);
        stringRedisTemplate.delete(tokenKey);
    }

    @PatchMapping("user")
    @RequiredToken
    public User user(@RequestBody User user, HttpServletRequest request) {
        // 从上下文中取到token
        String token = (String) request.getAttribute("token");
        // 从上下文中取到旧的用户信息
        User userOld = (User) request.getAttribute("user");
        // 判断是否要修改手机号
        if (!StringUtils.isEmpty(user.getPhone())){
            String phoneKey = "phone_" + user.getPhone();
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(phoneKey))) {
                throw new RuntimeException("提示: 验证码已过期");
            }
            String redisCaptcha = stringRedisTemplate.opsForValue().get(phoneKey);//获取redis验证码
            if (!StringUtils.equals(redisCaptcha, user.getCaptcha())) {
                throw new RuntimeException("提示:验证码输入错误!");
            }
            userOld.setPhone(user.getPhone());
        }
        //4.未修改手机号
        if (!StringUtils.isEmpty(user.getName())) {
            userOld.setName(user.getName());
        }
        if (!StringUtils.isEmpty(user.getIntro())) {
            userOld.setIntro(user.getIntro());
        }
        //5.更新用户信息
        userService.update(userOld);
        //6.更新redis中原始用户信息
        redisTemplate.opsForValue().set("session_" + token, userOld, 7, TimeUnit.DAYS);
        return userOld;
    }

    @PostMapping("/user/videos")
    @RequiredToken
    public Video publishVideos(MultipartFile file, Video video, Integer category_id, HttpServletRequest request) throws IOException {
        // 获取文件的原始姓名
        String originFileName = file.getOriginalFilename();
        // 获取文件后缀
        String ext = FilenameUtils.getExtension(originFileName);
        // 生成 UUID
        String uuidFileName = UUID.randomUUID().toString().replace("-", "");
        // 生成 uuid 文件名
        String newFileName = uuidFileName+"."+ext;
        // 上传阿里云 oss 返回文件所在oos地址
        String url = OSSUtils.upload(file.getInputStream(), "videos", newFileName);
        log.info("上传成功, 返回的地址为: {}", url);

        // 阿里云 oss 截取视频的一帧作为封面
        String cover = url + "?x-oss-process=video/snapshot,t_30000,f_jpg,w_0,h_0,m_fast,ar_auto";
        log.info("阿里云oss根据url截取视频封面: {}", cover);

        // 设置视频信息
        video.setCover(cover);
        video.setLink(url);
        video.setCategoryId(category_id);

        // 获取用户信息
        User user = (User) request.getAttribute("user");
        video.setUid(user.getId());

        // 调用视频服务
        Video videoResult = videoClient.publish(video);
        return videoResult;
    }

    // 增加播放次数
    @PutMapping("/user/played/{id}")
    public void played(@PathVariable("id") String videoId, HttpServletRequest request) {
        //redis中播放次数+1
        stringRedisTemplate.opsForValue().increment(RedisPrefix.VIDEO_PLAYED_COUNT_PREFIX + videoId);
        //1.获取登录用户
        User user = getUser(request);
        if (!ObjectUtils.isEmpty(user)) {
            //记录用户的播放历史
            Played played = new Played();
            played.setUid(user.getId());
            played.setVideoId(Integer.valueOf(videoId));
            played = playedService.insert(played);
            log.info("当前用户的播放记录保存成功,信息为: {}", JSONUtils.writeValueAsString(played));
        }
    }

    // 用户点赞视频
    @PutMapping("/user/liked/{id}")
    @RequiredToken
    public void liked(@PathVariable("id") String videoId, HttpServletRequest request) {
        // 获取当前登录的用户
        User user = (User) request.getAttribute("user");

        // 将视频点赞次数+1
        stringRedisTemplate.opsForValue().increment(RedisPrefix.VIDEO_LIKE_COUNT_PREFIX + videoId);

        // 将当前用户点赞视频列表放入redis中
        stringRedisTemplate.opsForSet().add(RedisPrefix.USER_LIKE_PREFIX + user.getId(), videoId);

        // 将不喜欢列表中的视频删除
        if (stringRedisTemplate.opsForSet().isMember(RedisPrefix.USER_DISLIKE_PREFIX + user.getId(), videoId)) {
            stringRedisTemplate.opsForSet().remove(RedisPrefix.USER_DISLIKE_PREFIX + user.getId(), videoId);
        }
    }

    /**
     * 用户取消点赞视频
     */
    @DeleteMapping("/user/liked/{id}")
    @RequiredToken
    public void cancelLiked(@PathVariable("id") Integer videoId, HttpServletRequest request) {
        //1.获取用户信息
        User user = (User) request.getAttribute("user");
        log.info("接收的到视频id: {}", videoId);

        //2.将当前用户喜欢的列表中该视频移除掉
        stringRedisTemplate.opsForSet().remove(RedisPrefix.USER_LIKE_PREFIX + user.getId(), videoId.toString());

        //3.将视频点赞次数-1
        stringRedisTemplate.opsForValue().decrement(RedisPrefix.VIDEO_LIKE_COUNT_PREFIX + videoId);
    }


    /**
     * 用户收藏视频
     */
    @PutMapping("/user/favorites/{id}")
    @RequiredToken
    public void createFavorites(@PathVariable("id") Integer videoId, HttpServletRequest request) {
        log.info("收藏的视频id: {}", videoId);
        //1.获取当前登陆用户信息
        User user = (User) request.getAttribute("user");
        //2.判断是否收藏该视频
        Favorite favorite = favoriteService.queryByVideoIdAndUserId(videoId, user.getId());
        if (ObjectUtils.isEmpty(favorite)) {
            favorite = new Favorite();
            favorite.setVideoId(videoId);
            favorite.setUid(user.getId());
            favorite = favoriteService.insert(favorite);
            log.info("收藏视频成功: {}", JSONUtils.writeValueAsString(favorite));
        }
    }

    /**
     * 用户取消收藏视频
     */
    @DeleteMapping("/user/favorites/{id}")
    @RequiredToken
    public void cancelFavorites(@PathVariable("id") Integer videoId, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        log.info("取消收藏的视频id: {}", videoId);
        int i = favoriteService.deleteByVideoIdAndUserId(videoId, user.getId());
        log.info("取消视频收藏成功:, {}", i > 0);
    }

    /**
     * 用户收藏列表
     */
    @GetMapping("/user/favorites")
    @RequiredToken
    public List<VideoVO> favorites(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        List<VideoVO> videoVOS = favoriteService.findFavoritesByUserId(user.getId());
        log.info("当前用户收藏的视频为: {}", JSONUtils.writeValueAsString(videoVOS));
        return videoVOS;
    }

    /**
     * 用户点击不喜欢
     */
    @PutMapping("/user/disliked/{id}")
    @RequiredToken
    public void disliked(@PathVariable("id") String videoId, HttpServletRequest request) {
        // 1.获取当前点击用户的信息
        User user = (User) request.getAttribute("user");

        //2.放入当前用户不喜欢的列表
        stringRedisTemplate.opsForSet().add(RedisPrefix.USER_DISLIKE_PREFIX + user.getId(), videoId);

        //3.判断之前是否点击过喜欢该视频
        if (stringRedisTemplate.opsForSet().isMember(RedisPrefix.USER_LIKE_PREFIX + user.getId(), videoId)) {
            stringRedisTemplate.opsForSet().remove(RedisPrefix.USER_LIKE_PREFIX + user.getId(), videoId);//从喜欢中列表中删除
            stringRedisTemplate.opsForValue().decrement(RedisPrefix.VIDEO_LIKE_COUNT_PREFIX + videoId);//当前视频喜欢次数-1
        }
    }
    /**
     * 用户取消不喜欢
     * Favorite favorite = userClient.favorite(uid,videoId);
     * if(!ObjectUtils.isEmpty(favorite))detail.setFavorite(true)
     */
    @DeleteMapping("/user/disliked/{id}")
    @RequiredToken
    public void cancelDisliked(@PathVariable("id") String videoId, HttpServletRequest request) {
        //1.获取当前用户信息
        User user = (User) request.getAttribute("user");
        //2.将当前视频从用户不喜欢的列表中移除掉
        if (stringRedisTemplate.opsForSet().isMember(RedisPrefix.USER_DISLIKE_PREFIX + user.getId(), videoId)) {
            stringRedisTemplate.opsForSet().remove(RedisPrefix.USER_DISLIKE_PREFIX + user.getId(), videoId);
        }
    }

    // 用户播放历史记录
    @GetMapping("/user/played")
    @RequiredToken
    public List<VideoVO> played(HttpServletRequest request, @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "per_page", defaultValue = "5") Integer rows) {
        log.info("当前页: {} 每页显示记录: {}", page, rows);
        User user = (User) request.getAttribute("user");
        List<VideoVO> videoVOS = playedService.queryByUserId(user.getId(), page, rows);
        log.info("当前用户播放历史的视频为: {}", JSONUtils.writeValueAsString(videoVOS));
        return videoVOS;
    }

    //根据请求参数中token获取用户信息
    private User getUser(HttpServletRequest request) {
        String token = request.getParameter("token");
        log.info("token为: {}", token);
        String tokenKey = "session_" + token;
        return (User) redisTemplate.opsForValue().get(tokenKey);
    }
}
