package netflix.dao.impl;

import netflix.dao.RatingModelDao;
import netflix.model.RatingModel;
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

    public Integer insert(RatingModel model) {
        Integer insertnum=ratingModelDao.insert(model);
        sqlSession.commit();
        return insertnum;
    }


    public Integer insertBatch(List<RatingModel> models) {
        Integer insertnum=ratingModelDao.insertBatch(models);
        sqlSession.commit();
        return insertnum;
    }

    public List<RatingModel> selectbyuserId(Integer userId){
        return ratingModelDao.selectbyuserId(userId);
    }
}
