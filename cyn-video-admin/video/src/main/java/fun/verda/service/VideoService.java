package fun.verda.service;

import fun.verda.dao.VideoDao;
import fun.verda.entity.Video;

import javax.annotation.Resource;
import java.util.List;

public interface VideoService {

    Long findTotalCountByKeywords(String id, String name, String categoryId, String username);

    List<Video> findAllByKeywords(Integer page, Integer rows, String id, String name, String categoryId, String username);
}
