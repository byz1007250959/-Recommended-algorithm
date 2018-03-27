package netflix;

import dataset.DataSetPath;

import java.io.*;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/3/27
 * @描述 : 这个类将netflix数据集的训练集中的17770个小文件合并成为一个大文件
 */
public class MergeTrainDataFile {
    private static String TrainDataDir=DataSetPath.NETFLIXPATH+"training";
    private static String OutputFilePath=DataSetPath.NETFLIXPATH+"merge_file_test.txt";

    /* *
     * @author duan
     * @描述  :这个方法将netflix数据集中的训练集(17770)个小文件合并成为一个大文件
     * 由于该文件夹下面的文件全部为小文件，不存在目录所以下面的代码中没有判断文件
     * 是否是目录文件，直接对文件进行操作。
     * @date 2018/3/27 10:10
     * @param null
     * @return void
     */
    public void mergeDataAndOutput(){
        Integer filenum;
        File dirfile=new File(TrainDataDir);
        if(dirfile.exists()){
            //合并文件不存在则创建，存在则清空
            File mergeFile=new File(OutputFilePath);
            if(!mergeFile.exists()){
                try {
                    mergeFile.createNewFile();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else {
                try {
                    FileWriter fileWriter=new FileWriter(mergeFile);
                    fileWriter.write("");
                    fileWriter.flush();
                    fileWriter.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
            File[] files=dirfile.listFiles();
            filenum=files.length;
            System.out.println("该文件夹下一共有:"+filenum+"个文件");
            try {
                FileWriter writer=new FileWriter(mergeFile);
                for(File file:files){
                    FileReader reader=new FileReader(file);
                    BufferedReader bufferedReader=new BufferedReader(reader);
                    String line;
                    while ((line=bufferedReader.readLine())!=null){
                        line+="\n";
                        writer.write(line);
                    }
                }
                writer.flush();
                writer.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
        else {
            System.out.println("文件夹不存在，操作失败");
        }
    }

    public static void main(String[] args){
        long a=System.currentTimeMillis();
        MergeTrainDataFile merge=new MergeTrainDataFile();
        merge.mergeDataAndOutput();
        long b=System.currentTimeMillis();
        System.out.println("合并所有文件花费的时间为:"+(b-a)+"毫秒");
    }
}
