package netflix.algorithm;

import dataset.DataSetPath;
import ml100k.model.ItemSimilarityModel;
import ml100k.model.RatingModel;
import ml100k.model.UserInterestLevel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/5/9
 *
 * @描述:取netflix的前1000部电影作为一个子数据集，数据集大小约为65m
 * 实现方式也依然选择将模型数据存入内存计算，与ml-100k实现方式相同
 */
public class ItemCfSubset {
    private int[][] itemsMixed=new int[1000][1000];
    private double[][] itemsSimilarity=new double[1000][1000];
    private static String outFilePath="D:/eva/outfile";
    private static Integer limitHistory=10;
    private static Integer limitMovie=20;
    private static Integer k=10;  //k用来记录和某个物品最相似的物品限制数
    public void recommendAlluser(){
        long a=System.currentTimeMillis();
        List<String> ratingsdata=readRatingFile(DataSetPath.NETFLIXPATH+"merge_file.txt");
        //创建模型
        List<RatingModel> ratingModels=getRatingData(ratingsdata);
        Map<String,Map<Integer,Object>> resultMap=createUserAndMovieMap(ratingModels);
        Map<Integer,Object> userRatingMap=resultMap.get("ratingMap");
        Map<Integer,Object> movieMap=resultMap.get("movieMap");
        calItemSimilarity(userRatingMap,movieMap);
        long c=System.currentTimeMillis();
        System.out.println("创建模型并且计算所有物品相似度花费时间"+(c-a)/1000+"秒");
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
            Set<Integer> userids=userRatingMap.keySet();
            System.out.println("一共要对"+userids.size()+"个用户推荐电影");
            int i=0;
            for(Integer id:userids){
                //long a=System.currentTimeMillis();
                i++;
                System.out.println("正在对第"+i+"个用户推荐电影");
                Set<Integer> recommedIds=recommendMoviesByUserid(id,limitHistory,limitMovie,k,userRatingMap);
                //long b=System.currentTimeMillis();
                //System.out.println("对用户"+i+"推荐花费时间:"+(b-a)+"毫秒");
                StringBuffer stringBuffer=new StringBuffer();
                stringBuffer.append(id);
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
        long b=System.currentTimeMillis();
        System.out.println("计算总时间花费"+(b-a)/1000+"秒");
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
            //先构造用户电影打分表
            Map<Integer,RatingModel> ratingMap;      //这个map记录该用户所有评分过的电影，key：movieid，value：rating
            if(userRatingMap.containsKey(userid)){
                /*
                该用户已存在userRatingMap中，取出该用户的打分map添加一条打分记录
                 */
                ratingMap=(Map<Integer, RatingModel>) userRatingMap.get(userid);
                ratingMap.put(movieid,model);
            }
            else {
                /*
                第一次遇到这个用户，在userRatingMap中添加一条记录
                 */
                ratingMap=new HashMap<>();
                ratingMap.put(movieid,model);
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
     * @描述  :这个函数计算所有
     * @date 2018/4/8 10:11
     * @param :用户电影打分map以及电影的倒排表
     * @return void
     */
    @SuppressWarnings("unchecked")
    private void calItemSimilarity(Map<Integer,Object> userRatingMap,Map<Integer,Object> movieMap){
        Map<Integer,RatingModel> ratingMap;      //这个map记录该用户所有评分过的电影，key：movieid，value：RatingTimeModel
        //首先计算同时喜欢物品x和物品y的用户交集矩阵
        for(Integer userid:userRatingMap.keySet()){
            ratingMap=(Map<Integer, RatingModel>) userRatingMap.get(userid);
            Set<Integer> seenMovies=ratingMap.keySet();
            List<Integer> movies=new ArrayList<>();
            for(Integer integer:seenMovies)
                movies.add(integer);
            //两两电影在相似度矩阵中加1
            for(int i=0;i<movies.size();i++){
                for(int j=i+1;j<movies.size();j++){
                    int movieId1=movies.get(i);
                    int movieId2=movies.get(j);
                    itemsMixed[movieId1-1][movieId2-1]+=1;
                }
            }
        }
        //接下来计算物品的相似度
        for(int movie1=0;movie1<1000;movie1++){
            for(int movie2=0;movie2<1000;movie2++){
                if(movie1==movie2||itemsMixed[movie1][movie2]==0)
                    continue;
                else{
                    Set<Integer> movie1users=(Set<Integer>) movieMap.get(movie1+1);
                    Set<Integer> movie2users=(Set<Integer>) movieMap.get(movie2+1);
                    itemsSimilarity[movie1][movie2]=itemsMixed[movie1][movie2]/Math.sqrt(movie1users.size()*movie2users.size()*1.0);
                }
            }
        }

    }

    /* *
     * @author duan
     * @描述  :这个函数根据用户id来进行电影的推荐，推荐的原则是选取用户近期看过的limitHistory部电影
     * 根据历史的电影来选择和这些电影最相似的k部电影，在选出的电影中计算用户对所有电影的感兴趣程度
     * 最终选择limitMovie部电影推荐给该用户 计算用户对某个物品感兴趣的程度使用公式
     * Puj=sum(Wji*rui) i属于N(u)和S(j,K)的交集，其中N(u)表示用户u喜欢的物品集合，S(j,K)表示
     * 和物品j最相似的k个物品 sum表示累加求和 Wji表示物品j对i的相似度rui表示用户u对物品i的打分情况
     * @date 2018/4/8 10:54
     * @param [userid, limitHistory, limitMovie,k, userRationMap]
     * @return java.util.List<java.lang.Integer>
     */
    @SuppressWarnings("unchecked")
    private Set<Integer> recommendMoviesByUserid(Integer userid,Integer limitHistory,Integer limitMovie,Integer k,
                                                 Map<Integer,Object> userRationMap){
        Set<Integer> recommendMovies=new HashSet<>();
        Map<Integer,RatingModel> ratingMap;
        ratingMap=(Map<Integer,RatingModel>)userRationMap.get(userid);
        //寻找该用户最近打分过的limitHistory部电影和用户看过的电影
        List<RatingModel> historyMovies=new ArrayList<>();
        Set<Integer> seenMovies=new HashSet<>();
        for(RatingModel model:ratingMap.values()){
            historyMovies.add(model);
            seenMovies.add(model.getMovidId());
        }
        Collections.sort(historyMovies,Collections.reverseOrder());
        List<RatingModel> limithistory;
        if(limitHistory<historyMovies.size())
            limithistory=historyMovies.subList(0,limitHistory);
        else
            limithistory=historyMovies;
        //下面从选中的电影中挑选最相似的k部电影放到一起
        Set<Integer> choiceMovies=new HashSet<>();
        for(RatingModel model:limithistory){
            Integer movieid=model.getMovidId();
            List<ItemSimilarityModel> similarityModels=calMostSimilarityKModels(movieid,k);
            for(ItemSimilarityModel model1:similarityModels){
                //    if(!seenMovies.contains(model1.getTargetItem()))
                choiceMovies.add(model1.getTargetItem());
            }
        }
        /*
        接下来对所有选择的电影计算该用户对该电影的感兴趣程度，选择用户最感兴趣的
        limitmovie部电影作为最终的推荐电影
         */
        List<UserInterestLevel> finalSortedLevel=calInterestLevelByOneUser(userid,seenMovies,choiceMovies,ratingMap,k);
        for(UserInterestLevel level:finalSortedLevel){
            recommendMovies.add(level.getMovieId());
            if(limitMovie.equals(recommendMovies.size()))
                break;
        }
        return  recommendMovies;
    }


    /* *
     * @author duan
     * @描述  :这个函数对所有选择的电影计算感兴趣程度，将结果排序后返回
     * @date 2018/4/8 15:00
     * @param [userSeenMovies, choiceMovies, userRatingMap]
     * @return java.util.List<ml100k.model.UserInterestLevel>
     */
    @SuppressWarnings("unchecked")
    private List<UserInterestLevel> calInterestLevelByOneUser(Integer userid,Set<Integer> userSeenMovies,Set<Integer> choiceMovies,
                                                              Map<Integer,RatingModel> userRatingMap,Integer k){
        List<UserInterestLevel> resultList=new ArrayList<>();
        for(Integer choice:choiceMovies){
            List<Integer> itemMixed=new ArrayList<>();
            List<ItemSimilarityModel> choiceSimilarities=calMostSimilarityKModels(choice,k);
            for(ItemSimilarityModel model:choiceSimilarities){
                if(userSeenMovies.contains(model.getTargetItem()))
                    itemMixed.add(model.getTargetItem());
            }
            if(!itemMixed.isEmpty()){
                double interest=0;
                for(Integer movieid:itemMixed){
                    RatingModel model=userRatingMap.get(movieid);
                    Integer rating=model.getRating();
                    interest+=rating*itemsSimilarity[choice-1][movieid-1];
                    UserInterestLevel interestLevel=new UserInterestLevel();
                    interestLevel.setUserId(userid);
                    interestLevel.setMovieId(choice);
                    interestLevel.setInterestLevel(interest);
                    resultList.add(interestLevel);
                }
            }
        }
        Collections.sort(resultList,Collections.reverseOrder());
        return  resultList;
    }

    /* *
     * @author duan
     * @描述  :这个函数计算和电影movieid最相似的前k部电影
     * @date 2018/4/8 14:47
     * @param [movieId]
     * @return java.util.List<ml100k.model.ItemSimilarityModel>
     */
    private List<ItemSimilarityModel> calMostSimilarityKModels(Integer movieId,Integer k){
        List<ItemSimilarityModel> itemSimilarityModels=new ArrayList<>();
        double theitemSimilarity[]=itemsSimilarity[movieId-1];
        for(int i=0;i<theitemSimilarity.length;i++){
            if(theitemSimilarity[i]==0)
                continue;
            ItemSimilarityModel model1=new ItemSimilarityModel();
            model1.setSimilarity(theitemSimilarity[i]);
            model1.setSourceItem(movieId);
            model1.setTargetItem(i+1);
            itemSimilarityModels.add(model1);
        }
        Collections.sort(itemSimilarityModels,Collections.reverseOrder());
        if(k>itemSimilarityModels.size())
            return  itemSimilarityModels;
        else
            return itemSimilarityModels.subList(0,k);
    }

    public static void main(String args[]){
        ItemCfSubset subset=new ItemCfSubset();
        subset.recommendAlluser();
    }

}
