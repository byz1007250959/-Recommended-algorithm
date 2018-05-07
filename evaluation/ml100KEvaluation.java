package evaluation;

import dataset.DataSetPath;
import ml100k.model.RatingModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created with IDEA
 * USER: DUAN
 * DATE: 2018/5/7.
 */
public class ml100KEvaluation {
    private static String sourceDataPath= DataSetPath.ML100KPATH+"u.data";
    private String outputPath; //算法推荐结果文件路径
    public ml100KEvaluation(String filePath){
        this.outputPath=filePath;
    }

    /* *
     * @author duan
     * @描述:评估过程封装
     * @date 2018/5/7 11:16
     * @param []
     * @return void
     */
    public void startEvaluation(){
        List<String> ratingsdata=readRatingFile(sourceDataPath);
        List<RatingModel> ratingModels=getRatingData(ratingsdata);
        Map<Integer,Object> userRatingMap=createUserRatingMap(ratingModels);
        List<String> outlines=readOutFile(outputPath);
        double coverage=calCoverage(outlines);
        System.out.println("覆盖率为:"+String.format("%.2f",coverage)+"%");
        double precision=calPrecision(outlines,userRatingMap);
        System.out.println("准确率为:"+String.format("%.2f",precision)+"%");
        double recall=calRecall(outlines,userRatingMap);
        System.out.println("召回率为:"+String.format("%.2f",recall)+"%");
    }

    /* *
     * @author duan
     * @描述:召回率的计算
     * @date 2018/5/7 11:15
     * @param [outlines, userRatingMap]
     * @return double
     */
    @SuppressWarnings("unchecked")
    private double calRecall(List<String> outlines,Map<Integer,Object> userRatingMap){
        int allcount=0;
        int hitcount=0;
        for(String line:outlines){
            String tokens[]=line.split("\t");
            Integer userid=Integer.valueOf(tokens[0]);
            String ids[]=tokens[1].split(",");
            Map<Integer,Integer> currentUserMap=(Map<Integer, Integer>) userRatingMap.get(userid);
            for(String movieid:ids){
                Integer recommendId=Integer.valueOf(movieid);
                if(currentUserMap.containsKey(recommendId))
                    hitcount++;
            }
            allcount+=currentUserMap.size();
        }
        return (hitcount/(allcount*1.0))*100;
    }


    /* *
     * @author duan
     * @描述:覆盖率计算
     * @date 2018/5/7 11:15
     * @param [outlines]
     * @return double
     */
    private double calCoverage(List<String> outlines){
        int itemCount=1682;
        Set<Integer> movieIds=new HashSet<>();
        for(String line:outlines){
            String tokens[]=line.split("\t");
            String ids[]=tokens[1].split(",");
            for(String id:ids){
                if(id!=null){
                    movieIds.add(Integer.valueOf(id));
                }
            }
        }
        return  (movieIds.size()/(itemCount*1.0))*100;
    }

    /* *
     * @author duan
     * @描述:准确率计算
     * @date 2018/5/7 11:15
     * @param [outlines, userRatingMap]
     * @return double
     */
    @SuppressWarnings("unchecked")
    private double calPrecision(List<String> outlines,Map<Integer,Object> userRatingMap){
        int recommendLength=0;
        int hitcount=0;
        for(String line:outlines){
            String tokens[]=line.split("\t");
            Integer userid=Integer.valueOf(tokens[0]);
            String ids[]=tokens[1].split(",");
            Map<Integer,Integer> currentUserMap=(Map<Integer, Integer>) userRatingMap.get(userid);
            for(String movieid:ids){
                Integer recommendId=Integer.valueOf(movieid);
                if(currentUserMap.containsKey(recommendId))
                    hitcount++;
            }
            recommendLength+=ids.length;
        }
        return (hitcount/(recommendLength*1.0))*100;
    }

    private List<String> readOutFile(String filepath){
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

    @SuppressWarnings("unchecked")
    public Map<Integer,Object> createUserRatingMap(List<RatingModel> ratingModels){
        Map<Integer,Object> userRatingMap=new HashMap<>();
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
        }
        return userRatingMap;
    }

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

    public static void main(String args[]){
        ml100KEvaluation evaluation=new ml100KEvaluation("D:/eva/outfile");
        evaluation.startEvaluation();
    }
}
