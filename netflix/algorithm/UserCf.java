package netflix.algorithm;

import dataset.DataSetPath;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/3/27
 * @描述 :这个类是user-based算法，与上一个不同的是这次处理的数据量巨大
 * 因此算法产生了一些变化
 * @实验结果: 由于数据集过于庞大，在读取文件并构造用户打分表和电影的倒排表的过程中就导致
 * 了内存的溢出。
 */
public class UserCf {

    /* *
     * @author duan
     * @描述  :这个函数用于构建计算流程中所需要的两种主要map：用户的观影打分map,电影的倒排表
     * @date 2018/3/27 14:30
     * @param   ：filepath
     * @return  ：Map结构，其中一个是用户电影的打分map，另一个是电影的倒排表
     */
    @SuppressWarnings("unchecked")
    public Map<String,Map<Integer,Object>> constructAllNeedMap(String dirpath){
        Map<String,Map<Integer,Object>> resultMap=new HashMap<>();
        Map<Integer,Object> userRatingMap=new HashMap<>();
        Map<Integer,Object> movieMap=new HashMap<>();
        try {
            File dirfile=new File(dirpath);
            if(!dirfile.exists()){
                System.out.println("没有找到训练集文件");
                return null;
            }
            File[] files=dirfile.listFiles();
            Integer filenum=files.length;
            System.out.println("该文件夹下一共有:"+filenum+"个文件");
            Integer num=1;
            for(File file:files){
                System.out.println("当前读取第"+num+"个文件");
                num++;
                //BufferedReader reader=new BufferedReader(new FileReader(file));
                Scanner scanner=new Scanner(new FileInputStream(file));
                Integer currentMovieId=0;
                //String line=reader.readLine();
                String line=scanner.nextLine();
                //每次读取一行来处理
                while (scanner.hasNextLine()){
                    if(line.endsWith(":")){
                        //这是一部新的电影的观影记录
                        String movieid=line.substring(0,line.length()-1);
                        currentMovieId=Integer.valueOf(movieid);
                        Set<Integer> userids=new HashSet<>();
                        movieMap.put(currentMovieId,userids);
                    }
                    else {
                        String[] fields=line.split(",");
                        //先在当前电影的观影记录表中添加该用户id
                        Integer currentUserId=Integer.valueOf(fields[0]);
                        Integer rating=Integer.valueOf(fields[1]);
                        Set<Integer> userids=(Set<Integer>) movieMap.get(currentMovieId);
                        userids.add(currentUserId);
                        //接下来判断用户打分map中是否存在该用户记录，存在则添加一条，不存在则创建后添加
                        Map<Integer,Integer> ratingMap;      //这个map记录该用户所有评分过的电影，key：movieid，value：rating
                        if(userRatingMap.containsKey(currentUserId)){
                    /*
                    该用户已存在userRatingMap中，取出该用户的打分map添加一条打分记录
                    */
                            ratingMap=(Map<Integer, Integer>) userRatingMap.get(currentUserId);
                            ratingMap.put(currentMovieId,rating);
                        }
                        else {
                        /*
                        第一次遇到这个用户，在userRatingMap中添加一条记录
                        */
                            ratingMap=new HashMap<>();
                            ratingMap.put(currentMovieId,rating);
                            userRatingMap.put(currentUserId,ratingMap);
                        }
                    }
                    //line=reader.readLine();
                    line=scanner.nextLine();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        resultMap.put("ratingMap",userRatingMap);
        resultMap.put("movieMap",movieMap);
        return  resultMap;
    }

    public static void main(String[] args){
        long a=System.currentTimeMillis();
        UserCf userCf=new UserCf();
        userCf.constructAllNeedMap(DataSetPath.NETFLIXPATH+"training_set");
        long b=System.currentTimeMillis();
        System.out.println("构建相关表消耗时间为:"+(b-a)+"毫秒");
    }

}
