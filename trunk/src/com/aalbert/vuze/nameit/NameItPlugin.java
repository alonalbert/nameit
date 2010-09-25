// Copyright 2010 Google. All Rights Reserved.
package com.aalbert.vuze.nameit;

import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.savelocation.SaveLocationManager;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.ui.UIManager;

/**
 * @author aalbert@google.com Alon Albert
 */
public class NameItPlugin implements Plugin {

  public void initialize(PluginInterface pluginInterface) throws PluginException {
    LoggerChannel loggerChannel = pluginInterface.getLogger().getChannel("AutoRename");
    final UIManager uiManager = pluginInterface.getUIManager();
    final SaveLocationManager oldSaveLocationManager =
        pluginInterface.getDownloadManager().getSaveLocationManager();
    pluginInterface.getDownloadManager().setSaveLocationManager(
        new MySaveLocationManager(oldSaveLocationManager));

  }

}
