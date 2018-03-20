package ml100k.model;

/**
 * Created by Administrator on 2018/3/20.
 */
public class RatingModel {
    private Integer userId;
    private Integer movidId;
    private Integer rating;
    private Integer timestamp;

    @Override
    public String toString() {
        return "RatingModel{" +
                "userId=" + userId +
                ", movidId=" + movidId +
                ", rating=" + rating +
                ", timestamp=" + timestamp +
                '}';
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getMovidId() {
        return movidId;
    }

    public void setMovidId(Integer movidId) {
        this.movidId = movidId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }
}
