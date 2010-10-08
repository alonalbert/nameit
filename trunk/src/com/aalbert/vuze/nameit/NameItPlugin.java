// Copyright 2010 Google. All Rights Reserved.
package com.aalbert.vuze.nameit;

import org.gudy.azureus2.core3.category.Category;
import org.gudy.azureus2.core3.category.CategoryListener;
import org.gudy.azureus2.core3.category.CategoryManager;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerException;
import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginConfig;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.savelocation.SaveLocationManager;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.logging.LoggerChannelListener;
import org.gudy.azureus2.plugins.ui.UIManager;
import org.gudy.azureus2.plugins.ui.components.UITextArea;
import org.gudy.azureus2.plugins.ui.model.BasicPluginViewModel;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author aalbert@google.com Alon Albert
 */
public class NameItPlugin implements Plugin {

  public void initialize(PluginInterface pluginInterface) throws PluginException {
    final SaveLocationManager oldSaveLocationManager =
        pluginInterface.getDownloadManager().getSaveLocationManager();
    final LoggerChannel logger = pluginInterface.getLogger().getTimeStampedChannel("NameIt");

    final UIManager uiManager = pluginInterface.getUIManager();
    final BasicPluginViewModel viewModel = uiManager.createBasicPluginViewModel("NameIt");
    uiManager.createBasicPluginConfigModel("NameIt")
        .addDirectoryParameter2("SeedingDir", "SeedingDir", "");
    logger.addListener(new MyLoggerChannelListener(viewModel.getLogArea()));

    logger.log(LoggerChannel.LT_INFORMATION, "Initializing NameIt plugin");
    pluginInterface.getDownloadManager().setSaveLocationManager(new MySaveLocationManager(
        logger, oldSaveLocationManager));

    final PluginConfig pluginconfig = pluginInterface.getPluginconfig();
    final Category seedingCategory = CategoryManager.getCategory("Seeding");
    if (seedingCategory != null) {
      seedingCategory.addCategoryListener(new CategoryListener() {
        public void downloadManagerAdded(Category cat, DownloadManager manager) {
          final String dir = pluginconfig.getPluginStringParameter("SeedingDir", null);
          if (dir != null && new File(dir).isAbsolute()) {
            try {
              final String name = manager.getDisplayName();
              manager.moveDataFiles(new File(dir), name);
              logger.log("Moved " + manager.getDisplayName() + " files to " + dir + "/" + name);
            } catch (DownloadManagerException e) {
              logger.log("Exception moving files to " + dir, e);
            }
          }
        }

        public void downloadManagerRemoved(Category cat, DownloadManager removed) {
        }
      });
    }
  }

  private static class MyLoggerChannelListener implements LoggerChannelListener {

    private final UITextArea logArea;

    public MyLoggerChannelListener(UITextArea logArea) {
      this.logArea = logArea;
    }

    public void
    messageLogged(int type, String message) {
      logArea.appendText(message + "\n");
    }

    public void messageLogged(String str, Throwable error) {
      logArea.appendText(str + "\n");
      StringWriter writer = new StringWriter();
      error.printStackTrace(new PrintWriter(writer));
      logArea.appendText(writer.toString() + "\n");
    }
  }
}
