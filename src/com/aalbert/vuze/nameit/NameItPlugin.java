// Copyright 2010 Google. All Rights Reserved.
package com.aalbert.vuze.nameit;

import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.savelocation.SaveLocationManager;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.logging.LoggerChannelListener;
import org.gudy.azureus2.plugins.ui.UIManager;
import org.gudy.azureus2.plugins.ui.components.UITextArea;
import org.gudy.azureus2.plugins.ui.model.BasicPluginViewModel;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author aalbert@google.com Alon Albert
 */
public class NameItPlugin implements Plugin {

  public void initialize(PluginInterface pluginInterface) throws PluginException {
    final SaveLocationManager oldSaveLocationManager =
        pluginInterface.getDownloadManager().getSaveLocationManager();
    final LoggerChannel logger = pluginInterface.getLogger().getChannel("NameIt");

    final UIManager uiManager = pluginInterface.getUIManager();
    final BasicPluginViewModel viewModel = uiManager.createBasicPluginViewModel("NameIt");
		
    logger.addListener(
      new LoggerChannelListener() {
        public void
        messageLogged(int type, String message) {
          viewModel.getLogArea().appendText( message+"\n");
        }

        public void messageLogged(String str, Throwable	error) {
          final UITextArea logArea = viewModel.getLogArea();
          logArea.appendText( str + "\n");
          StringWriter writer = new StringWriter();
          error.printStackTrace(new PrintWriter(writer));
          viewModel.getLogArea().appendText(writer.toString() + "\n");
        }
      });

    logger.log(LoggerChannel.LT_INFORMATION, "Initializing NameIt plugin");
    pluginInterface.getDownloadManager().setSaveLocationManager(new MySaveLocationManager(
        logger, oldSaveLocationManager));

  }

}
