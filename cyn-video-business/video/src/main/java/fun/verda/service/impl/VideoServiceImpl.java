package fun.verda.service.impl;


import fun.verda.dao.VideoDao;
import fun.verda.entity.Category;
import fun.verda.entity.Favorite;
import fun.verda.entity.User;
import fun.verda.entity.Video;
import fun.verda.feignclients.CategoryClients;
import fun.verda.feignclients.UserClients;
import fun.verda.service.VideoService;
import fun.verda.utils.JSONUtils;
import fun.verda.utils.RedisPrefix;
import fun.verda.vo.VideoDetailVO;
import fun.verda.vo.VideoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {
    @Resource
    private VideoDao videoDao;

    @Resource
    private UserClients userClients;

    @Resource
    private CategoryClients categoryClients;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate<String, User> redisTemplate;


    @Override
    public Video insert(Video video) {
        video.setCreatedAt(new Date());//设置创建日期
        video.setUpdatedAt(new Date());//设置更新日期
        videoDao.insert(video);

        // TODO add video info to es
        rabbitTemplate.convertAndSend("videos", "", JSONUtils.writeValueAsString(video));

        return video;
    }

    @Override
    public List<VideoVO> queryAllByLimit(Integer offset, Integer limit) {
        offset = (offset - 1) * limit;
        //1.分页查询所有视频对象
        List<Video> videos = this.videoDao.queryAllByLimit(offset, limit);
        return getList(videos);
    }

    @Override
    public List<VideoVO> findAllByCategoryId(Integer page, Integer rows, Integer categoryId) {
        int start = (page - 1) * rows;
        List<Video> videos = videoDao.findAllByCategoryId(start, rows, categoryId);
        return getList(videos);
    }

    @Override
    public VideoDetailVO queryDetailById(String videoId, String token) {
        //1.根据id查询视频信息
        Video video = videoDao.queryById(Integer.valueOf(videoId));

        //2.创建videoDetailVo
        VideoDetailVO videoDetailVO = new VideoDetailVO();

        //3.将video对象中基本信息复制到VideoDetailVo {id title cover link created_at update_at}
        BeanUtils.copyProperties(video, videoDetailVO);

        //4.设置当前视频类别名称  VideoDetailVo {id title cover link created_at update_at category}
        Category category = categoryClients.category(video.getCategoryId().toString());//调用类别服务
        log.info("根据类别id查询到的类别信息为: {}", JSONUtils.writeValueAsString(category));
        videoDetailVO.setCategory(category.getName());


        //5.设置用户信息 VideoDetailVo {id title cover link created_at update_at category uploader}
        User user = userClients.user(video.getUid().toString());//调用用户服务
        log.info("根据id查询用户信息为: {}", JSONUtils.writeValueAsString(video));
        videoDetailVO.setUploader(user);

        //6.设置播放次数 {id title cover link created_at update_at category uploader plays_count}
        videoDetailVO.setPlaysCount(0);//初始化默认值为0
        String playedCounts = stringRedisTemplate.opsForValue().get(RedisPrefix.VIDEO_PLAYED_COUNT_PREFIX + videoId);
        if (!StringUtils.isEmpty(playedCounts)) {
            log.info("当前视频视频播放次数为: {}", playedCounts);
            videoDetailVO.setPlaysCount(Integer.valueOf(playedCounts));
        }


        //7.设置点赞数 {id title cover link created_at update_at category uploader plays_count likes_count}
        videoDetailVO.setLikesCount(0);//初始化默认值为0
        String likedCounts = stringRedisTemplate.opsForValue().get(RedisPrefix.VIDEO_LIKE_COUNT_PREFIX + video.getId());
        if (!StringUtils.isEmpty(likedCounts)) {
            log.info("当前视频点赞数量为: {}", likedCounts);
            videoDetailVO.setLikesCount(Integer.valueOf(likedCounts));
        }


        //8.设置当前用户是否喜欢  不喜欢  收藏 {id title cover link created_at update_at category uploader plays_count likes_count liked  disabled favorite}
        User o = (User) redisTemplate.opsForValue().get("session_" + token);
        if (!ObjectUtils.isEmpty(o)) {
            //设置是否喜欢
            Boolean liked = stringRedisTemplate.opsForSet().isMember(RedisPrefix.USER_LIKE_PREFIX + o.getId(), videoId);
            videoDetailVO.setLiked(liked);
            //设置是否不喜欢
            Boolean disliked = stringRedisTemplate.opsForSet().isMember(RedisPrefix.USER_DISLIKE_PREFIX + o.getId(), videoId);
            videoDetailVO.setDisliked(disliked);
            //设置是否收藏
            Favorite favorite = userClients.favorite(videoId, o.getId().toString());
            log.info("是否收藏过该视频: {}", !ObjectUtils.isEmpty(favorite));
            if (!ObjectUtils.isEmpty(favorite)) {
                videoDetailVO.setFavorite(true);
            }
        }

        return videoDetailVO;
    }


    // 通过id查询视频信息
    @Override
    public VideoVO queryById(Integer id) {
        return getVideoVO(videoDao.queryById(id));
    }

    //将list video 转为 list videoVO
    public List<VideoVO> getList(List<Video> videos) {
        //创建VideoVo集合
        List<VideoVO> videoVOS = new ArrayList<>();
        //对video进行遍历 在遍历过程中转为videoVo
        videos.forEach(video -> {
            //video{id title intro cover uid category_id create_at ...}   //videoVo{id title cover category likes uploader create_at}
            VideoVO videoVO = getVideoVO(video);

            videoVOS.add(videoVO);//添加视频
        });
        return videoVOS;
    }

    public VideoVO getVideoVO(Video video) {
        //创建VideoVo
        VideoVO videoVO = new VideoVO();
        //复制属性
        BeanUtils.copyProperties(video, videoVO);//复制属性

        //视频服务----->调用用户服务 根据用户id查询用户
        User user = userClients.user(video.getUid().toString());//调用用户服务
        videoVO.setUploader(user.getName());//设置用户名

        //视频服务---->调用类别服务  根据类别id查询类别
        Category category = categoryClients.category(video.getCategoryId().toString());
        videoVO.setCategory(category.getName());

        //设置点赞数量
        videoVO.setLikes(0);

//        String counts = stringRedisTemplate.opsForValue().get(RedisPrefix.VIDEO_LIKE_COUNT_PREFIX + video.getId());
//        if (!StringUtils.isEmpty(counts)) {
//            log.info("当前视频点赞数量为: {}", counts);
//            videoVO.setLikes(Integer.valueOf(counts));
//        }
        return videoVO;
    }
}
