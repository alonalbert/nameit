// Copyright 2010 Google. All Rights Reserved.
package com.aalbert.vuze.nameit;

import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.savelocation.SaveLocationChange;
import org.gudy.azureus2.plugins.download.savelocation.SaveLocationManager;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author aalbert@google.com Alon Albert
*/
class MySaveLocationManager
    implements org.gudy.azureus2.plugins.download.savelocation.SaveLocationManager {

  private static final String REGEX = "(.*)\\.[Ss](\\d+)[Ee](\\d+)\\..*\\.(mkv|avi|mpg|mp4|wmv)";
  private static final Pattern pattern = Pattern.compile(REGEX);
  private final SaveLocationManager oldManager;

  public MySaveLocationManager(SaveLocationManager oldManager) {
    this.oldManager = oldManager;
  }

  public SaveLocationChange onInitialization(
      Download download, boolean for_move, boolean on_event) {
    return oldManager.onInitialization(download, for_move, on_event);
  }

  public SaveLocationChange onCompletion(Download download, boolean for_move, boolean on_event) {
    final SaveLocationChange change = createNewSaveLocationChange(download);
    SaveLocationChange oldChange = oldManager.onCompletion(download, for_move, on_event);
    if (oldChange == null) {
      oldChange = new SaveLocationChange();
    }
    if (change != null) {
      change.torrent_location = oldChange.torrent_location;
      return change;
    } else {
      return oldChange;
    }
  }

  public SaveLocationChange onRemoval(Download download, boolean for_move, boolean on_event) {
    return oldManager.onRemoval(download, for_move, on_event);
  }

  private SaveLocationChange createNewSaveLocationChange(Download download) {
    final File file = getEpisodeFile(download);
    if (file == null) {
      return null;
    }
    final EpisodeMatch episodeMatch = getEpisodeMatch(file.getName());
    if (episodeMatch == null) {
      return null;
    }

    String showName = episodeMatch.getShowName();
    final int seasonNum = episodeMatch.getSeasonNum();
    final int episodeNum = episodeMatch.getEpisodeNum();
    final String ext = episodeMatch.getExt();

    final EpisodeInfo episodeInfo = new EpisodeInfo(showName, seasonNum, episodeNum);
    final String episodeName = episodeInfo.getEpisodeName();
    showName = episodeInfo.getShowName();

    SaveLocationChange change = oldManager.onCompletion(download, false, false);

    final String parent;
    if (change != null) {
      parent = change.download_location.getAbsolutePath();
    } else {
      change = new SaveLocationChange();
      parent = file.getParent();
    }
    change.download_location = new File(parent + "/TV", showName);
    change.download_name =
        String.format("%02d-%02d %s.%s", seasonNum, episodeNum, episodeName, ext);
    change.torrent_name =
        String.format("%s %02d-%02d %s", showName, seasonNum, episodeNum, episodeName);

    return change;
  }

  private EpisodeMatch getEpisodeMatch(String fileName) {
    final Matcher matcher = pattern.matcher(fileName);
    if (!matcher.matches()) {
      return null;
    }

    String showName = matcher.group(1).replace('.', ' ');
    int seasonNum = Integer.parseInt(matcher.group(2));
    int episodeNum = Integer.parseInt(matcher.group(3));
    String ext = matcher.group(4);

    return new EpisodeMatch(showName, seasonNum, episodeNum, ext);
  }

  private File getEpisodeFile(Download download) {
    final DiskManagerFileInfo[] fileInfos = download.getDiskManagerFileInfo();
    if (fileInfos.length != 1) {
      // only useful for single file torrents because Vuze cannot rename files in a
      // multiple file torrent
      return null;
    }
    return fileInfos[0].getFile();
  }
}
