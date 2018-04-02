package netflix.dao;

import netflix.model.RatingModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/3/30
 */
public interface RatingModelDao {
    public Integer insert(RatingModel model);
    public List<RatingModel> selectbyuserId(@Param("userId") Integer userId);
    public Integer insertBatch(List<RatingModel> models);
    public List<RatingModel> selectbyMovieId(@Param("movieId") Integer movieId);
}
