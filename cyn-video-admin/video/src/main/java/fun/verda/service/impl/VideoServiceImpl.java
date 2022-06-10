package fun.verda.service.impl;

import fun.verda.dao.VideoDao;
import fun.verda.entity.Video;
import fun.verda.service.VideoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class VideoServiceImpl implements VideoService {

    @Resource
    private VideoDao videoDao;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<Video> findAllByKeywords(Integer offset, Integer limit, String id, String name, String categoryId, String username) {
        int start = (offset - 1) * limit;
        return videoDao.findAllByKeywords(start, limit, id, name, categoryId, username);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Long findTotalCountByKeywords(String id, String name, String categoryId, String username) {
        return videoDao.findTotalCountByKeywords(id, name, categoryId, username);
    }
}
