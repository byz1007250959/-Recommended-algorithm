package ml100k.model;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/3/23
 */
public class UserInterestLevel implements Comparable<UserInterestLevel>{
    private Integer userId;
    private Integer movieId;
    private Double interestLevel;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public Double getInterestLevel() {
        return interestLevel;
    }

    public void setInterestLevel(Double interestLevel) {
        this.interestLevel = interestLevel;
    }

    @Override
    public int compareTo(UserInterestLevel o) {
        double otherlevel=o.getInterestLevel();
        double mylevel=this.getInterestLevel();
        if(mylevel-otherlevel>0)
            return 1;
        else if(mylevel-otherlevel<0)
            return -1;
        else
            return 0;

    }

    @Override
    public String toString() {
        return "UserInterestLevel{" +
                "userId=" + userId +
                ", movieId=" + movieId +
                ", interestLevel=" + interestLevel +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInterestLevel that = (UserInterestLevel) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (movieId != null ? !movieId.equals(that.movieId) : that.movieId != null) return false;
        return interestLevel != null ? interestLevel.equals(that.interestLevel) : that.interestLevel == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (movieId != null ? movieId.hashCode() : 0);
        result = 31 * result + (interestLevel != null ? interestLevel.hashCode() : 0);
        return result;
    }
}
