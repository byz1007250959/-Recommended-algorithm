package netflix.algorithm;

import netflix.dao.impl.RatingModelDaoImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/4/9
 *
 * @描述: netflix数据集的itemcf算法，在计算相似度的过程中内存不足失败
 * 原因：电影的数量较多，有17770部电影，即使用int二维数组在内存中保存交集
 * 都会占用很大内存，后续计算还有一个保存在double中的二维数组也需要很大内存。
 * 如下代码采用map的结构保存交集信息。数组的下标表示电影的id+1，hashmap中保存
 * 该电影和其他电影的交集个数：比如下标1 的map中一条数据为 key：3 value：4
 * 表示同时喜欢电影id为2和电影id为3的电影的用户数目为4。但是这种表示方法仍然
 * 导致超出对内存限制，可能原因是平均每一部电影拥有的相似电影数量比较多，在加上
 * map对象等数据结构的表示仍然占用了极大内存，比如平均一部电影与上千部电影有交集
 * 即使都用int保存也需要几百mb内存，然后实际使用内存将远远超出这个数值。
 */
public class ItemCf {
    private Map []itemsMixed=new HashMap[17770];
    private RatingModelDaoImpl ratingModelDao=new RatingModelDaoImpl();

    /* *
     * @author duan
     * @描述  :这个函数计算电影之间的相似度(只有用户同时喜欢电影x和y电影x和y才有相似度否则不计算)
     * @date 2018/4/9 14:35
     * @param []
     * @return void
     */
    @SuppressWarnings("unchecked")
    private void  calItemSimilarity(){
        /*
        首先找出所有用户id，然后遍历每一个用户看过的电影进行计算
         */
        List<Integer> allUser=ratingModelDao.selectAllUserId();
        for(Integer currentUser:allUser){
            List<Integer> movies=ratingModelDao.selectAllMoviesByOneUser(currentUser);
            //接下来对这个用户看过的电影两两之间在交集map中+1
            for(int i=0;i<movies.size();i++){
                int movieId1=movies.get(i);
                for(int j=i+1;j<movies.size();j++){
                    int movieId2=movies.get(j);
                    if(itemsMixed[movieId1-1]==null){
                        Map<Integer,Integer> map=new HashMap<>();
                        map.put(movieId2,1);
                        itemsMixed[movieId1-1]=map;
                    }
                    else {
                        Map<Integer,Integer> map=itemsMixed[movieId1-1];
                        if(map.get(movieId2)!=null){
                            int value=map.get(movieId2);
                            value++;
                            map.put(movieId2,value);
                        }
                        else {
                            map.put(movieId2,1);
                        }
                    }
                }
            }
        }
    }

    public void test(){
        Map<Integer,Integer> integerIntegerMap=new HashMap<>();
        itemsMixed[17769]=integerIntegerMap;
        Map<Integer,Integer> arrayList=itemsMixed[17769];
    }

    public static void main(String []args){
        ItemCf itemCf=new ItemCf();
        long a=System.currentTimeMillis();
        itemCf.calItemSimilarity();
        long b=System.currentTimeMillis();
        System.out.println("计算物品相似度花费时间"+(b-a)/1000+"秒");
    }
}
