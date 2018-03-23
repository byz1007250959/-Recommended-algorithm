package ml100k.model;

/**
 * Created by Administrator on 2018/3/20.
 */
public class MovieModel {
    private Integer movieId;
    private String movieTitle;
    private String releaseDate;
    private String videlReleaseDate;
    private String url;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getVidelReleaseDate() {
        return videlReleaseDate;
    }

    public void setVidelReleaseDate(String videlReleaseDate) {
        this.videlReleaseDate = videlReleaseDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "MovieModel{" +
                "movieId=" + movieId +
                ", movieTitle='" + movieTitle + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", videlReleaseDate='" + videlReleaseDate + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
