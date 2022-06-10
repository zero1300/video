package fun.verda.service.impl;


import fun.verda.dao.FavoriteDao;
import fun.verda.entity.Favorite;
import fun.verda.feignClient.VideoClient;
import fun.verda.service.FavoriteService;
import fun.verda.vo.VideoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏(Favorite)表服务实现类
 *
 * @author chenyn
 * @since 2020-11-12 17:56:35
 */
@Service("favoriteService")
@Transactional
public class FavoriteServiceImpl implements FavoriteService {
    @Resource
    private FavoriteDao favoriteDao;

    @Autowired
    private VideoClient videosClient;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Favorite queryById(Integer id) {
        return this.favoriteDao.queryById(id);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<Favorite> queryAllByLimit(int offset, int limit) {
        return this.favoriteDao.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param favorite 实例对象
     * @return 实例对象
     */
    @Override
    public Favorite insert(Favorite favorite) {
        favorite.setCreatedAt(new Date());
        favorite.setUpdatedAt(new Date());
        this.favoriteDao.insert(favorite);
        return favorite;
    }

    /**
     * 修改数据
     *
     * @param favorite 实例对象
     * @return 实例对象
     */
    @Override
    public Favorite update(Favorite favorite) {
        this.favoriteDao.update(favorite);
        return this.queryById(favorite.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.favoriteDao.deleteById(id) > 0;
    }

    /**
     * 取消收藏
     *
     * @param videoId
     * @param userId
     */
    @Override
    public int deleteByVideoIdAndUserId(Integer videoId, Integer userId) {
        return this.favoriteDao.deleteByVideoIdAndUserId(videoId, userId);
    }


    /**
     * 判断当前用户是否收藏过视频
     *
     * @param videoId
     * @param userId
     * @return
     */
    @Override
    public Favorite queryByVideoIdAndUserId(Integer videoId, Integer userId) {
        return this.favoriteDao.queryByVideoIdAndUserId(videoId, userId);
    }

    /**
     * 查询用户收藏列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<VideoVO> findFavoritesByUserId(Integer userId) {
        List<Favorite> favorites = this.favoriteDao.queryByUserId(userId);
//        List<Integer> ids = new ArrayList<>();
//        favorites.forEach(favorite -> ids.add(favorite.getVideoId()));
        List<Integer> ids = favorites.stream().map(favorite -> favorite.getVideoId()).collect(Collectors.toList());
        return videosClient.getVideos(ids);
    }
}