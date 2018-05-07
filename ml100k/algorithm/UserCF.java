package ml100k.algorithm;

import dataset.DataSetPath;
import ml100k.model.MovieModel;
import ml100k.model.NeighborModel;
import ml100k.model.RatingModel;
import ml100k.model.UserInterestLevel;

import java.io.*;
import java.util.*;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/3/20
 */
public class UserCF {
    private static Integer limitNeighbor=10;
    private static Integer limitMovie=20;
    private static String outFilePath="D:/eva/outfile";
    public void recommendAllUser(){
        long c=System.currentTimeMillis();
        List<String> ratingsdata=readRatingFile(DataSetPath.ML100KPATH+"u.data");
        //创建模型
        List<RatingModel> ratingModels=getRatingData(ratingsdata);
        Map<String,Map<Integer,Object>> resultMap=createUserAndMovieMap(ratingModels);
        Map<Integer,Object> userRatingMap=resultMap.get("ratingMap");
        Map<Integer,Object> movieMap=resultMap.get("movieMap");
        File outfile=new File(outFilePath);
        if(!outfile.exists()){
            try {
                outfile.createNewFile();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            try {
                FileWriter fileWriter=new FileWriter(outfile);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            FileWriter writer=new FileWriter(outfile);
            for(int i=1;i<=943;i++){
                //long a=System.currentTimeMillis();
                System.out.println("正在对用户"+i+"推荐电影");
                List<Integer> recommedIds=recommendMoviesByUserid(i,limitNeighbor,limitMovie,userRatingMap,movieMap);
                //long b=System.currentTimeMillis();
                //System.out.println("对用户"+i+"推荐花费时间:"+(b-a)+"毫秒");
                StringBuffer stringBuffer=new StringBuffer();
                stringBuffer.append(i);
                stringBuffer.append("\t");
                for(Integer movieId:recommedIds){
                    stringBuffer.append(movieId);
                    stringBuffer.append(",");
                }
                stringBuffer.append("\n");
                writer.write(stringBuffer.toString());
            }
            writer.flush();
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        long d=System.currentTimeMillis();
        System.out.println("计算总时间花费"+(d-c)/1000+"秒");
    }

    /* *
     * @author duan
     * @描述  :这个方法封装了整个算法流程，从创建模型到推荐结果的展示
     * @date 2018/3/23 16:25
     * @param ：无
     * @return void
     */
    public void userCfAlgorithm(){
        long a=System.currentTimeMillis();
        //读取原始数据
        List<String> moviesdata=readMoviesInfo(DataSetPath.ML100KPATH+"u.item");
        List<String> ratingsdata=readRatingFile(DataSetPath.ML100KPATH+"u.data");
        //创建模型
        List<MovieModel> movieModels=getMovieData(moviesdata);
        List<RatingModel> ratingModels=getRatingData(ratingsdata);
        Map<Integer,MovieModel> movieInfoMap=createMovieInfoMap(movieModels);
        Map<String,Map<Integer,Object>> resultMap=createUserAndMovieMap(ratingModels);
        Map<Integer,Object> userRatingMap=resultMap.get("ratingMap");
        Map<Integer,Object> movieMap=resultMap.get("movieMap");
        long b=System.currentTimeMillis();
        System.out.println("创建模型花费时间:"+(b-a));
        //进行推荐
        List<Integer> recommendMovies=recommendMoviesByUserid(234,10,20,userRatingMap,movieMap);
        long c=System.currentTimeMillis();
        System.out.println("为一个用户计算推荐电影花费时间:"+(c-b));
        //展示推荐结果
        for(Integer movieid:recommendMovies){
            MovieModel recommendMovie=movieInfoMap.get(movieid);
            System.out.println("推荐电影名："+recommendMovie.getMovieTitle()+"  发布时间:"+recommendMovie.getReleaseDate()+
            "  观看地址:"+recommendMovie.getUrl());
        }
    }

    /* *
     * @author duan
     * @描述  :此方法将u.item文件按照行读入
     * @date 2018/3/23 16:10
     * @param ：文件路径
     * @return ：字符串数组ArrayList
     */
    private List<String> readMoviesInfo(String filepath){
        List<String> lines=new ArrayList<>();
        try {
            BufferedReader reader=new BufferedReader(new FileReader(new File(filepath)));
            String line;
            while ((line=reader.readLine())!=null){
                lines.add(line);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  lines;
    }

    /* *
     * @author duan
     * @描述  :此方法将读入的电影信息格式化为MovieModel
     * @date 2018/3/23 16:13
     * @param  ：字符串链表
     * @return  ：电影模型链表
     */
    private List<MovieModel> getMovieData(List<String> lines){
        List<MovieModel> movieModels=new ArrayList<>();
        for(String line:lines){
            MovieModel model=new MovieModel();
            String[] fields=line.split("\\|");
            model.setMovieId(Integer.valueOf(fields[0]));
            model.setMovieTitle(fields[1]);
            model.setReleaseDate(fields[2]);
            model.setVidelReleaseDate(fields[3]);
            model.setUrl(fields[4]);
            movieModels.add(model);
        }
        return  movieModels;
    }


    /* *
     * @author duan
     * @描述  :此方法将u.data文件中的数据按行读入，每一行记录了用户id，电影id，用户对电影打分，打分时间的信息
     * @date 2018/3/20 14:36
     * @param ：输入参数为文件的路径
     * @return ：返回一个字符串数组ArrayList
     */
    private List<String> readRatingFile(String filepath){
        List<String> lines=new ArrayList<>();
        try {
            BufferedReader reader=new BufferedReader(new FileReader(new File(filepath)));
            String line;
            while ((line=reader.readLine())!=null){
                lines.add(line);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  lines;
    }

    /* *
     * @author duan
     * @描述  将从文件读入的数据按照\t分割产生RatingModel的模型数据
     * @date 2018/3/20 14:59
     * @param :字符串链表
     * @return :模型链表
     */
    private List<RatingModel> getRatingData(List<String> lines){
        List<RatingModel> ratingModels=new ArrayList<>();
        for(String line:lines){
            RatingModel model=new RatingModel();
            String[] fields=line.split("\t");
            model.setUserId(Integer.valueOf(fields[0]));
            model.setMovidId(Integer.valueOf(fields[1]));
            model.setRating(Integer.valueOf(fields[2]));
            model.setTimestamp(Integer.valueOf(fields[3]));
            ratingModels.add(model);
        }
        return  ratingModels;
    }

    /* *
     * @author duan
     * @描述  :此方法构建电影信息的map表结构，用于最终将推荐电影的id转化为具体的电影信息
     * @date 2018/3/23 16:31
     * @param   ：List<MovieModel>
     * @return  :Map<Integer,MovieModel>  key:电影id  value：该电影的模型
     */
    private Map<Integer,MovieModel> createMovieInfoMap(List<MovieModel> movieModels){
        Map<Integer,MovieModel> resultMap=new HashMap<>();
        for(MovieModel movieModel:movieModels){
            resultMap.put(movieModel.getMovieId(),movieModel);
        }
        return  resultMap;
    }

    /* *
     * @author duan
     * @描述  此函数生成需要操作的数据结构：1：用户-电影的打分map，记录了用户对看过的电影的打分记录
     * 2：电影的倒排表，记录某一部电影被那些用户打分过。
     * @date 2018/3/20 15:57
     * @param RationModel的数组
     * @return  Map结构，其中一个是用户电影的打分map，另一个是电影的倒排表
     */
    @SuppressWarnings("unchecked")
    private Map<String,Map<Integer,Object>> createUserAndMovieMap(List<RatingModel> ratingModels){
        Map<String,Map<Integer,Object>> resultMap=new HashMap<>();
        Map<Integer,Object> userRatingMap=new HashMap<>();
        Map<Integer,Object> movieMap=new HashMap<>();
        for(RatingModel model:ratingModels){
            //获取模型数据
            Integer userid=model.getUserId();
            Integer movieid=model.getMovidId();
            Integer rating=model.getRating();
            //先构造用户电影打分表
            Map<Integer,Integer> ratingMap;      //这个map记录该用户所有评分过的电影，key：movieid，value：rating
            if(userRatingMap.containsKey(userid)){
                /*
                该用户已存在userRatingMap中，取出该用户的打分map添加一条打分记录
                 */
                ratingMap=(Map<Integer, Integer>) userRatingMap.get(userid);
                ratingMap.put(movieid,rating);
            }
            else {
                /*
                第一次遇到这个用户，在userRatingMap中添加一条记录
                 */
                ratingMap=new HashMap<>();
                ratingMap.put(movieid,rating);
                userRatingMap.put(userid,ratingMap);
            }

            //构造影片倒排表
            Set<Integer> userids;        //这个list记录了看过该电影的所有用户id
            if(movieMap.containsKey(movieid)){
                /*
                该电影已经添加到倒排表中
                 */
                userids=(Set<Integer>) movieMap.get(movieid);
                userids.add(userid);
            }
            else {
                /*
                第一次出现该电影
                 */
                userids=new TreeSet<>();
                userids.add(userid);
                movieMap.put(movieid,userids);
            }

        }
        resultMap.put("ratingMap",userRatingMap);
        resultMap.put("movieMap",movieMap);
        return  resultMap;
    }

    /* *
     * @author duan
     * @描述 ：这个函数计算某一个用户的邻居列表，邻居指的是该用户和某些其他用户对某些电影产生了交集,
     * 在这些和该用户产生交集的用户中函数计算两两用户的相似度，最后排序后进行返回
     * @date 2018/3/21 12:00
     * @param  当前用户id：userid，用户打分的map表：userRationMap，电影的倒排表：movieMap
     * @return   返回该用户的邻居列表
     */
    @SuppressWarnings("unchecked")
    private List<NeighborModel> calNeighbors(Integer userid,Map<Integer,Object> userRationMap,Map<Integer,Object> movieMap){
        List<NeighborModel> neighborModels=new ArrayList<>();
        Set<Integer> neighborIds=new HashSet<>();
        //获取当前用户的电影评分信息
        Map<Integer,Integer> currentUserRatingMap=(Map<Integer, Integer>) userRationMap.get(userid);
        for(Map.Entry<Integer,Integer> entry:currentUserRatingMap.entrySet()){
            /*
            遍历每一部电影，从电影倒排表中找出对该影片评价过的其他用户
             */
            Integer movieId=entry.getKey();
            Set<Integer> userids=(Set<Integer>) movieMap.get(movieId);
            for(Integer currentuser:userids){
                if(!currentuser.equals(userid)){
                    neighborIds.add(currentuser);
                }
            }
        }
        //遍历邻居集合，计算该用户和其他所有邻居的相似度
        for(Integer neighborid:neighborIds){
            if(neighborid.equals(userid))
                continue;
            Map<Integer,Integer> neighborRatingMap=(Map<Integer, Integer>) userRationMap.get(neighborid);
            Double similarity=calSimilarity(currentUserRatingMap,neighborRatingMap);
            NeighborModel neighborModel=new NeighborModel();
            neighborModel.setSimilarity(similarity);
            neighborModel.setUserId(userid);
            neighborModel.setNeighborId(neighborid);
            neighborModels.add(neighborModel);
        }
        Collections.sort(neighborModels,Collections.reverseOrder());
        return  neighborModels;
    }

    /* *
     * @author duan
     * @描述  一下函数计算2个用户之间的相似度，采用公式为：|A&B|/sqrt(|A || B |)
     * @date 2018/3/21 12:40
     * @param ;入参为两个用户看过的电影打分map
     * @return 返回为这两个用户的相似度，类型为Double
     */
    private Double calSimilarity(Map<Integer,Integer> user1,Map<Integer,Integer> user2){
        Double similarity;
        int mixedMovies=0;
        double unionMovies;
        for(Integer user1Movieid:user1.keySet()){
            for(Integer user2Movieid:user2.keySet()){
                if(user1Movieid.equals(user2Movieid)){
                    mixedMovies++;
                }
            }
        }
        unionMovies=user1.keySet().size()*user2.keySet().size()*1.0;
        similarity=mixedMovies/Math.sqrt(unionMovies);
        return  similarity;
    }

    /* *
     * @author duan
     * @描述  此函数为某个用户推荐电影，推荐的电影来自他的最近的k个邻居，并且是该用户没有看过的电影
     * @date 2018/3/23 11:15
     * @param 用户id：userid ，最近的k邻居个数：limitNeighbor，推荐的电影数目限制：limitMovie
     * 用户打分的map表：userRationMap，电影的倒排表：movieMap
     * @return 返回推荐的电影id列表
     */
    @SuppressWarnings("unchecked")
    private   List<Integer> recommendMoviesByUserid(Integer userid,Integer limitNeighbor,Integer limitMovie,
                                                  Map<Integer,Object> userRationMap,Map<Integer,Object> movieMap){
        List<Integer> recommendMovies=new ArrayList<>();
        //计算该用户的邻居
        List<NeighborModel> neighborModels=calNeighbors(userid,userRationMap,movieMap);
        //将最相似的邻居用户转换成map结构方便后面计算，Map<Integer,Double>  key：邻居id  value：相似度
        Map<Integer,Double> similarityMap=new HashMap<>();
        for(NeighborModel model:neighborModels){
            similarityMap.put(model.getNeighborId(),model.getSimilarity());
        }
        Set<Integer> allRecommendMovies=new HashSet<>();
        //Set<Integer> seenMovies=new HashSet<>();
        //寻找该用户看过的电影
//        Map<Integer,Integer> myRationgMap=(Map<Integer, Integer>) userRationMap.get(userid);
//        for(Integer key:myRationgMap.keySet()){
//            seenMovies.add(key);
//        }
        //寻找邻居看过的而用户没有看过的电影
        Set<Integer> nearestNeighbor=new HashSet<>();
        for(int i=0;i<limitNeighbor;i++){
            NeighborModel theNeighbor=neighborModels.get(i);
            Integer neighborId=theNeighbor.getNeighborId();
            nearestNeighbor.add(neighborId);
            Map<Integer,Integer> theNeighborMap=(Map<Integer, Integer>) userRationMap.get(neighborId);
            for(Integer key:theNeighborMap.keySet()){
            //    if(!seenMovies.contains(key)){
                    allRecommendMovies.add(key);
            //    }
            }
        }
        List<UserInterestLevel> finalSortedLevel=calInterestLevelByOneUser(userid,allRecommendMovies,nearestNeighbor,userRationMap,movieMap,similarityMap);
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
     * @date 2018/3/23 11:46
     * @param   用户id，所有待计算的推荐电影id，最近的k个邻居列表
     * 用户打分的map表：userRationMap，电影的倒排表：movieMap,该用户和他的邻居相似度Map
     * @return 所有待推荐电影的感兴趣程度排名，返回类型为UserInterestLevel
     */
    @SuppressWarnings("unchecked")
    private List<UserInterestLevel> calInterestLevelByOneUser(Integer userid,Set<Integer> recommendMovies,Set<Integer> nearestNeighbor,
                                                             Map<Integer,Object> userRationMap,Map<Integer,Object> movieMap,Map<Integer,Double> similarityMap){
        List<UserInterestLevel> resultList=new ArrayList<>();
        //遍历每一部推荐的电影，计算当前用户对该电影的感兴趣程度
        for(Integer movieId:recommendMovies){
            List<Integer> userMixed=new ArrayList<>();  //这个链表保存最近的邻居用户和看过该电影的用户的交集
            Set<Integer> userIds=(Set<Integer>) movieMap.get(movieId);
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
                Map<Integer,Integer> theRationMap=(Map<Integer, Integer>) userRationMap.get(neighborId);
                Integer theRating=theRationMap.get(movieId);
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

    public  static void main(String args[]){
        UserCF userCF=new UserCF();
        userCF.recommendAllUser();
    }

}
