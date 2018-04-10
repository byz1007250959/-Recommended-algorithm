package netflix.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/3/30
 */
public interface RatingModelDao {
    public List<Integer> selectMovieIdsByuserId(@Param("userId") Integer userId,@Param("limit") Integer globalLimit);
    public List<Integer> selectUserIdsByMovieId(@Param("movieId") Integer movieId,@Param("limit") Integer globalLimit);
    public Integer selectRatingByuserIdAndMovieid(@Param("userId") Integer userId,@Param("movieId") Integer movieId);
    public List<Integer> selectAllUserId();
    public List<Integer> selectAllMoviesByOneUser(@Param("userId") Integer userId);
}
