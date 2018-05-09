package ml100k.model;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/4/8
 *
 * @描述:
 */
public class ItemSimilarityModel implements Comparable<ItemSimilarityModel> {
    private Integer sourceItem;
    private Integer targetItem;
    private Double Similarity;

    @Override
    public int compareTo(ItemSimilarityModel o) {
        return this.getSimilarity().compareTo(o.getSimilarity());
//        Double thisSim=this.getSimilarity();
//        Double thatSim=o.getSimilarity();
//        if(thisSim-thatSim>0)
//            return 1;
//        else if(thisSim-thatSim<0)
//            return -1;
//        else
//            return 0;
    }

    @Override
    public String toString() {
        return "ItemSimilarityModel{" +
                "sourceItem=" + sourceItem +
                ", targetItem=" + targetItem +
                ", Similarity=" + Similarity +
                '}';
    }

    public Integer getSourceItem() {
        return sourceItem;
    }

    public void setSourceItem(Integer sourceItem) {
        this.sourceItem = sourceItem;
    }

    public Integer getTargetItem() {
        return targetItem;
    }

    public void setTargetItem(Integer targetItem) {
        this.targetItem = targetItem;
    }

    public Double getSimilarity() {
        return Similarity;
    }

    public void setSimilarity(Double similarity) {
        Similarity = similarity;
    }
}
