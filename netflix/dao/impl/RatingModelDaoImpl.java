package netflix.dao.impl;

import netflix.dao.RatingModelDao;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.Reader;
import java.util.List;
/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/3/30
 */
public class RatingModelDaoImpl {
    private static final String RESOURCE="mybatis-config.xml";
    private SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;
    private RatingModelDao ratingModelDao;
    /* *
    * @author duan
    * @描述 :构造函数中加载并获得查询表t_user_rating的dao实例
    * @date 2018/3/30 14:21
    * @param []
    * @return
    */
    public RatingModelDaoImpl(){
        try {
            Reader reader= Resources.getResourceAsReader(RESOURCE);
            sqlSessionFactory=new SqlSessionFactoryBuilder().build(reader);
            sqlSession=sqlSessionFactory.openSession();
            ratingModelDao=sqlSession.getMapper(RatingModelDao.class);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    public List<Integer> selectMovieIdsByuserId(Integer userId,Integer globalLimit){
        return ratingModelDao.selectMovieIdsByuserId(userId,globalLimit);
    }

    public List<Integer> selectUserIdsByMovieId(Integer movidId,Integer globalLimit){
        return ratingModelDao.selectUserIdsByMovieId(movidId,globalLimit);
    }

    public Integer selectRatingByuserIdAndMovieid(Integer userid,Integer movieid){
        return ratingModelDao.selectRatingByuserIdAndMovieid(userid,movieid);
    }

    public List<Integer> selectAllUserId(){
        return  ratingModelDao.selectAllUserId();
    }

    public List<Integer> selectAllMoviesByOneUser(Integer userid){
        return ratingModelDao.selectAllMoviesByOneUser(userid);
    }
}
