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
  private final SaveLocationManager oldSaveLocationManager;

  public MySaveLocationManager(SaveLocationManager oldSaveLocationManager) {
    this.oldSaveLocationManager = oldSaveLocationManager;
  }

  public SaveLocationChange onInitialization(
      Download download, boolean for_move, boolean on_event) {
    final DiskManagerFileInfo[] fileInfos = download.getDiskManagerFileInfo();
    if (fileInfos.length != 1) {
      // only useful for single file torrents because Vuze cannot rename files in a
      // multiple file torrent
      return null;
    }
    final File file = fileInfos[0].getFile();
    final Matcher matcher = pattern.matcher(file.getName());
    if (!matcher.matches()) {
      return null;
    }

    String showName = matcher.group(1).replace('.', ' ');
    int seasonNum = Integer.parseInt(matcher.group(2));
    int episodeNum = Integer.parseInt(matcher.group(3));
    String ext = matcher.group(4);

    final EpisodeInfo episodeInfo = new EpisodeInfo(showName, seasonNum, episodeNum);
    return null;
  }

  public SaveLocationChange onCompletion(Download download, boolean for_move, boolean on_event) {
    return createNewSaveLocationChange(download);
  }

  public SaveLocationChange onRemoval(Download download, boolean for_move, boolean on_event) {
    return null;
  }

  private SaveLocationChange createNewSaveLocationChange(Download download) {
    final DiskManagerFileInfo[] fileInfos = download.getDiskManagerFileInfo();
    if (fileInfos.length != 1) {
      // only useful for single file torrents because Vuze cannot rename files in a
      // multiple file torrent
      return null;
    }
    final File file = fileInfos[0].getFile();
    final Matcher matcher = pattern.matcher(file.getName());
    if (!matcher.matches()) {
      return null;
    }

    String showName = matcher.group(1).replace('.', ' ');
    int seasonNum = Integer.parseInt(matcher.group(2));
    int episodeNum = Integer.parseInt(matcher.group(3));
    String ext = matcher.group(4);

    final EpisodeInfo episodeInfo = new EpisodeInfo(showName, seasonNum, episodeNum);
    final String episodeName = episodeInfo.getEpisodeName();
    showName = episodeInfo.getShowName();

    SaveLocationChange change = oldSaveLocationManager.onCompletion(download, false, false);
    final String filename =
        String.format("%02d-%02d %s.%s", seasonNum, episodeNum, episodeName, ext);

    final String parent;
    if (change != null) {
      parent = change.download_location.getAbsolutePath();
    } else {
      change = new SaveLocationChange();
      parent = file.getParent();
    }
    change.download_location = new File(parent + "/TV", showName);
    change.download_name = filename;

    return change;
  }
}
