package netflix.algorithm;

import dataset.DataSetPath;
import ml100k.model.NeighborModel;
import ml100k.model.UserInterestLevel;
import netflix.dao.impl.RatingModelDaoImpl;
import netflix.model.MovieModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/3/27
 * @描述 :这个类是user-based算法，与上一个不同的是这次处理的数据量巨大
 * 因此算法产生了一些变化。对于这个数据集，由于数据集过于庞大，在读取文
 * 件并构造用户打分表和电影的倒排表的过程中就导致了内存的溢出。所以决定
 * 使用mysql数据库将用户对电影的打分数据存入数据库中，以便在之后算法流程
 * 中使用sql语句来获得需要的信息。
 */
public class UserCf {
    private RatingModelDaoImpl ratingModelDao=new RatingModelDaoImpl();
    private static Integer DEFAULT_CAL_MOVIES_NUM=100;
    /* *
     * @author duan
     * @描述  :这个函数构建电影的详细信息map表，用来最后给用户推荐详细的结果
     * @date 2018/3/27 14:30
     * @param   ：movieInfoData
     * @return  ：Map结构表示电影的详细信息表
     */
    @SuppressWarnings("unchecked")
    public Map<Integer,MovieModel> constructMovieInfoMap(String dataFilePath){
        Map<Integer,MovieModel> movieInfoMap=new HashMap<>();
        try {
            File datafile=new File(dataFilePath);
            if(!datafile.exists()){
                System.out.println("没有找到指定文件");
                return null;
            }
            BufferedReader reader=new BufferedReader(new FileReader(datafile));
            String line;
            while ((line=reader.readLine())!=null){
                String[] fields=line.split(",");
                Integer movidId=Integer.valueOf(fields[0]);
                MovieModel model=new MovieModel();
                model.setMovieId(movidId);
                model.setReleaseDate(fields[1]);
                model.setTitle(fields[2]);
                movieInfoMap.put(movidId,model);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  movieInfoMap;
    }

    /* *
     * @author duan
     * @描述  :这个函数为一个特定的用户推荐电影，推荐的电影来自他的最近的k个邻居
     * 并且只给他推荐他可能感兴趣的排名前limitMovie部电影
     * @date 2018/4/2 14:36
     * @param  userid：用户ID，limitNeighbor：最大邻居数,limitMovie：最大推荐电影，globallimit：查询时按照评分返回
     * 用户看过的前limit部电影
     * @return  推荐的电影id列表
     */
    private   List<Integer> recommendMoviesByUserid(Integer userid,Integer limitNeighbor,Integer limitMovie,Integer globalLimit){
        List<Integer> recommendMovies=new ArrayList<>();
        //获取当前用户的电影评分信息
        List<Integer> seenMovieslist=ratingModelDao.selectMovieIdsByuserId(userid,DEFAULT_CAL_MOVIES_NUM);
        //计算该用户的邻居
        System.out.println("开始计算相似度");
        List<NeighborModel> neighborModels=calNeighbors(userid,seenMovieslist,globalLimit);
        //将最相似的邻居用户转换成map结构方便后面计算，Map<Integer,Double>  key：邻居id  value：相似度
        Map<Integer,Double> similarityMap=new HashMap<>();
        for(NeighborModel model:neighborModels){
            similarityMap.put(model.getNeighborId(),model.getSimilarity());
        }
        Set<Integer> allRecommendMovies=new HashSet<>();
        Set<Integer> seenMovies=new HashSet<>();
        for(Integer movieid:seenMovieslist){
            seenMovies.add(movieid);
        }
        //寻找邻居看过的而用户没有看过的电影
        Set<Integer> nearestNeighbor=new HashSet<>();
        for(int i=0;i<limitNeighbor;i++){
            if(i==neighborModels.size())
                break;
            NeighborModel theNeighbor=neighborModels.get(i);
            Integer neighborId=theNeighbor.getNeighborId();
            nearestNeighbor.add(neighborId);
            List<Integer> theNeighbormovies=ratingModelDao.selectMovieIdsByuserId(neighborId,DEFAULT_CAL_MOVIES_NUM);
            for(Integer movieId:theNeighbormovies){
                if(!seenMovies.contains(movieId)){
                    allRecommendMovies.add(movieId);
                }
            }
        }
        System.out.println("开始计算推荐度");
        List<UserInterestLevel> finalSortedLevel=calInterestLevelByOneUser(userid,allRecommendMovies,nearestNeighbor,similarityMap,globalLimit);
        for(int i=0;i<limitMovie;i++){
            UserInterestLevel interestLevel=finalSortedLevel.get(i);
            recommendMovies.add(interestLevel.getMovieId());
        }
        return  recommendMovies;
    }


    /* *
     * @author duan
     * @描述  这个函数计算当前用户userid对某个推荐物品(在本数据集中为电影)的感兴趣程度
     * 计算公式为：p(u,i)=SUM(Wuv*Rvi) v属于与用户u最相似的k个邻居和看过电影i的用户的交集
     * Wuv表示用户u和v的相似度，Rvi表示用户v对电影i的评分
     * @date 2018/4/2 15:14
     * @param [userid, recommendMovies, nearestNeighbor, similarityMap]
     * @return java.util.List<ml100k.model.UserInterestLevel>
     */
    private List<UserInterestLevel> calInterestLevelByOneUser(Integer userid,Set<Integer> recommendMovies,
                                                              Set<Integer> nearestNeighbor,Map<Integer,Double> similarityMap,Integer globalLimit){
        List<UserInterestLevel> resultList=new ArrayList<>();
        //遍历每一部推荐的电影，计算当前用户对该电影的感兴趣程度
        for(Integer movieId:recommendMovies){
            List<Integer> userMixed=new ArrayList<>();  //这个链表保存最近的邻居用户和看过该电影的用户的交集
            List<Integer> seenMovieusers=ratingModelDao.selectUserIdsByMovieId(movieId,globalLimit);
            Set<Integer> userIds=new HashSet<>();
            for(Integer theuser:seenMovieusers){
                userIds.add(theuser);
            }
            for(Integer neighbor:nearestNeighbor){
                if(userIds.contains(neighbor)){
                    userMixed.add(neighbor);
                }
            }
            //没有交集无法计算感兴趣度跳过进行下一个电影计算
            if(userMixed.isEmpty())
                continue;
            //开始计算用户对这部电影的感兴趣程度
            double interest=0;
            for(Integer neighborId:userMixed){
                Integer theRating=ratingModelDao.selectRatingByuserIdAndMovieid(neighborId,movieId);
                Double similarity=similarityMap.get(neighborId);
                if(similarity!=null&&theRating!=null)
                    interest+=similarityMap.get(neighborId)*theRating;
            }
            UserInterestLevel interestLevel=new UserInterestLevel();
            interestLevel.setUserId(userid);
            interestLevel.setInterestLevel(interest);
            interestLevel.setMovieId(movieId);
            resultList.add(interestLevel);
        }
        Collections.sort(resultList,Collections.reverseOrder());
        return  resultList;
    }

    /* *
     * @author duan
     * @描述  :计算某个用户的邻居
     * @date 2018/4/2 14:41
     * @param [userid]，list<Integer> models：用户看过的电影id
     * @return java.util.List<ml100k.model.NeighborModel>
     */
    private  List<NeighborModel> calNeighbors(Integer userid,List<Integer> seenMovies,Integer globalLimit ){
        List<NeighborModel> neighborModels=new ArrayList<>();
        Set<Integer> neighborIds=new HashSet<>();
        for(Integer movieId:seenMovies){
            //寻找存在交集的用户
            List<Integer> seenMovieUsers=ratingModelDao.selectUserIdsByMovieId(movieId,globalLimit);
            for(Integer currentUserId:seenMovieUsers){
                if(!currentUserId.equals(userid)){
                    neighborIds.add(currentUserId);
                }
            }
        }
        //遍历邻居集合，计算该用户和其他所有邻居的相似度
        for(Integer neighborid:neighborIds){
            List<Integer> neighborSeenMovies=ratingModelDao.selectMovieIdsByuserId(neighborid,DEFAULT_CAL_MOVIES_NUM);
            Double similarity=calSimilarity(seenMovies,neighborSeenMovies);
            NeighborModel neighborModel=new NeighborModel();
            neighborModel.setSimilarity(similarity);
            neighborModel.setUserId(userid);
            neighborModel.setNeighborId(neighborid);
            neighborModels.add(neighborModel);
        }
        Collections.sort(neighborModels,Collections.reverseOrder());
        return neighborModels;
    }


    /* *
     * @author duan
     * @描述  :这个函数计算两个用户之间的相似度，采用公式为：|A&B|/sqrt(|A || B |)
     * @date 2018/4/2 14:59
     * @param [user1, user2]
     * @return java.lang.Double
     */
    private Double calSimilarity(List<Integer> user1,List<Integer> user2){
        Double similarity;
        int mixedMovies=0;
        double unionMovies;
        for(Integer user1movie:user1){
            for(Integer user2movie:user2){
                if(user1movie.equals(user2movie)){
                    mixedMovies++;
                }
            }
        }
        unionMovies=user1.size()*user2.size()*1.0;
        similarity=mixedMovies/Math.sqrt(unionMovies);
        return  similarity;
    }

    public static void main(String[] args){

        UserCf userCf=new UserCf();
        Map<Integer,MovieModel> movieInfoMap=userCf.constructMovieInfoMap(DataSetPath.NETFLIXPATH+"movie_titles.txt");
        long a=System.currentTimeMillis();
        List<Integer> recommendMovies=userCf.recommendMoviesByUserid(124105,5,20,20);
        long b=System.currentTimeMillis();
        System.out.println("为一个用户计算推荐电影花费时间:"+(b-a)/1000+"秒"); //展示推荐结果
        for(Integer movieid:recommendMovies){
            MovieModel recommendMovie=movieInfoMap.get(movieid);
            System.out.println("推荐电影名："+recommendMovie.getTitle()+"  发布时间:"+recommendMovie.getReleaseDate());
        }
    }

}
