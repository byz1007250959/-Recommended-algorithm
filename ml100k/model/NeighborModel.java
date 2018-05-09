package ml100k.model;

/**
 * Created with IDEA
 * USER: DUAN
 * DATE: 2018/3/21.
 */
public class NeighborModel implements Comparable<NeighborModel>{
    private Integer userId;
    private Double similarity;
    private Integer neighborId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    public Integer getNeighborId() {
        return neighborId;
    }

    public void setNeighborId(Integer neighborId) {
        this.neighborId = neighborId;
    }


    @Override
    public int compareTo(NeighborModel o) {
        return this.getSimilarity().compareTo(o.getSimilarity());
//        double otherSimilarity=o.getSimilarity();
//        double mySimilarity=this.getSimilarity();
//        if(mySimilarity-otherSimilarity>0)
//            return 1;
//        else if(mySimilarity-otherSimilarity<0)
//            return -1;
//        else
//            return 0;
    }

    @Override
    public String toString() {
        return "NeighborModel{" +
                "userId=" + userId +
                ", similarity=" + similarity +
                ", neighborId=" + neighborId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NeighborModel that = (NeighborModel) o;

        if (!userId.equals(that.userId)) return false;
        if (!similarity.equals(that.similarity)) return false;
        return neighborId.equals(that.neighborId);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + similarity.hashCode();
        result = 31 * result + neighborId.hashCode();
        return result;
    }
}
