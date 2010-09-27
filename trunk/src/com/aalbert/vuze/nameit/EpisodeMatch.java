package com.aalbert.vuze.nameit;

/**
 * Created by IntelliJ IDEA. User: al Date: Sep 26, 2010 Time: 8:10:06 AM To change this template
 * use File | Settings | File Templates.
 */
public class EpisodeMatch {

  private final String showName;
  private final int seasonNum;
  private final int episodeNum;
  private final String ext;

  public EpisodeMatch(String showName, int seasonNum, int episodeNum, String ext) {

    this.showName = showName;
    this.seasonNum = seasonNum;
    this.episodeNum = episodeNum;
    this.ext = ext;
  }

  public String getShowName() {
    return showName;
  }

  public int getSeasonNum() {
    return seasonNum;
  }

  public int getEpisodeNum() {
    return episodeNum;
  }

  public String getExt() {
    return ext;
  }
}
