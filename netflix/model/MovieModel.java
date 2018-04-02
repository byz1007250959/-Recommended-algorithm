package netflix.model;

/**
 * Created with IDEA
 * USER: Administrator
 * DATE: 2018/4/2
 *
 * @描述:
 */
public class MovieModel {
    private Integer movieId;
    private String releaseDate;
    private String title;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "MovieModel{" +
                "movieId=" + movieId +
                ", releaseDate='" + releaseDate + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
