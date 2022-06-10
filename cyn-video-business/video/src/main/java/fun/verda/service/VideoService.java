package fun.verda.service;

import fun.verda.entity.Video;
import fun.verda.vo.VideoDetailVO;
import fun.verda.vo.VideoVO;

import java.util.List;

public interface VideoService {
    Video insert(Video video);

    List<VideoVO> queryAllByLimit(Integer page, Integer per_page);

    List<VideoVO> findAllByCategoryId(Integer page, Integer rows, Integer categoryId);

    VideoDetailVO queryDetailById(String videoId, String token);

    VideoVO queryById(Integer id);
}
