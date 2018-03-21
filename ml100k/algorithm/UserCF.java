package ml100k.algorithm;

import dataset.DataSetPath;
import ml100k.model.NeighborModel;
import ml100k.model.RatingModel;

import java.io.*;
import java.util.*;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/3/20
 */
public class UserCF {


    /* *
     * @author duan
     * @描述  :此方法将u.data文件中的数据按行读入，每一行记录了用户id，电影id，用户对电影打分，打分时间的信息
     * @date 2018/3/20 14:36
     * @param ：输入参数为文件的路径
     * @return ：返回一个字符串数组ArrayList
     */
    public List<String> readRatingFile(String filepath){
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
    public List<RatingModel> getRatingData(List<String> lines){
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
     * @描述  此函数生成需要操作的数据结构：1：用户-电影的打分map，记录了用户对看过的电影的打分记录
     * 2：电影的倒排表，记录某一部电影被那些用户打分过。
     * @date 2018/3/20 15:57
     * @param RationModel的数组
     * @return  Map结构，其中一个是用户电影的打分map，另一个是电影的倒排表
     */
    public Map<String,Map<Integer,Object>> createUserAndMovieMap(List<RatingModel> ratingModels){
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
    public List<NeighborModel> calNeighbors(Integer userid,Map<Integer,Object> userRationMap,Map<Integer,Object> movieMap){
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
                if(currentuser!=userid){
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
    public Double calSimilarity(Map<Integer,Integer> user1,Map<Integer,Integer> user2){
        Double similarity;
        int mixedMovies=0;
        double unionMovies=0;
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

    public  static void main(String args[]){
        UserCF userCF=new UserCF();
        List<String> lines=userCF.readRatingFile(DataSetPath.ML100KPATH+"u.data");
        List<RatingModel> models=userCF.getRatingData(lines);
        Map<String,Map<Integer,Object>> resultMap=userCF.createUserAndMovieMap(models);
        Map<Integer,Object> userRatingMap=resultMap.get("ratingMap");
        Map<Integer,Object> movieMap=resultMap.get("movieMap");
        List<NeighborModel> neighborModels=userCF.calNeighbors(196,userRatingMap,movieMap);
        for(NeighborModel neighborModel:neighborModels){
            neighborModel.getSimilarity();
        }
    }

}
