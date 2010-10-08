// Copyright 2010 Google. All Rights Reserved.
package com.aalbert.vuze.nameit;

import com.sun.org.apache.xpath.internal.NodeSet;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author aalbert@google.com Alon Albert
 */
public class EpisodeInfo {
  private static final String API_KEY = "B007F67F2D4B5FF3";
  private static final String MASTER_URL = "http://www.thetvdb.com";
  private static final String MIRROR_URL = MASTER_URL + "/api/" + API_KEY + "/mirrors.xml";

  private static final int MIRROR_TYPE_XML = 1;

  private static final XPath xpath = XPathFactory.newInstance().newXPath();
  private static final Random random = new Random(new Date().getTime());

  private static final Map<String, String> fixShowNames = new HashMap<String, String>();

  private String showName;
  private String episodeName;
  private String showImageUrl;
  private String episodeImageUrl;
  private Map<String, String> art = new HashMap<String, String>();

  static {
    fixShowNames.put("tosh 0", "Tosh.0");
    fixShowNames.put("shit my dad says", "$#*! My Dad Says");
    fixShowNames.put("penn and teller bullshit", "Penn & Teller: Bullshit!");
  }

  public EpisodeInfo(LoggerChannel logger, String showName, int seasonNum, int episodeNum) {
    String fixed = fixShowNames.get(showName.toLowerCase());
    if (fixed != null) {
      showName = fixed;
    }
    this.showName = showName;
    episodeName = String.format("Episode %d", episodeNum);


    String uri;
    Document document;
    NodeList nodes;
    try {
      final DocumentBuilder documentBuilder =
          DocumentBuilderFactory.newInstance().newDocumentBuilder();
      final String mirror = getMirror(documentBuilder);

      uri = mirror + "/api/GetSeries.php?seriesname=" + URLEncoder.encode(showName, "UTF-8");
      document = documentBuilder.parse(uri);
      nodes = (NodeList) xpath.evaluate("/Data/Series", document, XPathConstants.NODESET);

      if (nodes.getLength() == 0) {
        logger.log("No shows found for " + uri);
        return;
      }

      Element series = null;
      if (nodes.getLength() == 1) {
        series = (Element) nodes.item(0);
        this.showName = series.getElementsByTagName("SeriesName").item(0).getTextContent();
      } else {
        for (int i = 0; i < nodes.getLength(); ++i) {
          Element candidate = (Element) nodes.item(i);
          final String name = candidate.getElementsByTagName("SeriesName").item(0).getTextContent();
          if (showName.equalsIgnoreCase(name)) {
            series = candidate;
            this.showName = name;
            break;
          }
        }
        if (series == null) {
          logger.log("No shows found for " + uri);
          return;
        }
      }

      final String seriesId = series.getElementsByTagName("seriesid").item(0).getTextContent();

      String xpath;
      // get series image
      uri = String.format("%s/api/B007F67F2D4B5FF3/series/%s/en.xml", mirror, seriesId);
      document = documentBuilder.parse(uri);
      xpath = "/Data/Series/poster/text()";
      showImageUrl = String.format(
          "%s/banners/%s",
          mirror,
          EpisodeInfo.xpath.evaluate(xpath, document, XPathConstants.STRING));

      // get all artwork
      uri = String.format("%s/api/B007F67F2D4B5FF3/series/%s/banners.xml", mirror, seriesId);
      document = documentBuilder.parse(uri);
      xpath = "/Banners/Banner/BannerPath";
      NodeList banners = (NodeList) EpisodeInfo.xpath.evaluate(xpath, document, XPathConstants.NODESET);
      for (int i = 0; i < banners.getLength(); i++) {
        final String path = banners.item(i).getTextContent();
        art.put(path.replace('/', '_'), String.format("%s/banners/%s", mirror, path));
      }

      // get episode data
      uri = String.format("%s/api/B007F67F2D4B5FF3/series/%s/default/%d/%d",
                          mirror, seriesId, seasonNum, episodeNum);
      document = documentBuilder.parse(uri);
      xpath = "/Data/Episode/EpisodeName/text()";
      String name = (String) EpisodeInfo.xpath.evaluate(xpath, document, XPathConstants.STRING);

      if (name == null) {
        logger.log("No shows found for " + uri);
        return;
      }

      episodeName = name;

      xpath = "/Data/Episode/filename/text()";
      episodeImageUrl = String.format(
          "%s/banners/%s",
          mirror,
          EpisodeInfo.xpath.evaluate(xpath, document, XPathConstants.STRING));
    } catch (Exception e) {
      logger.log("Exception processing '" + showName + " " + seasonNum + "/" + episodeNum, e);
    }
  }

  public String getShowName() {
    return showName;
  }

  public String getShowImageUrl() {
    return showImageUrl;
  }

  public String getEpisodeName() {
    return episodeName;
  }

  public String getEpisodeImageUrl() {
    return episodeImageUrl;
  }

  public Map<String, String> getArt() {
    return art;
  }

  private String getMirror(DocumentBuilder documentBuilder) {
    String mirror;
    try {
      final Document document = documentBuilder.parse(MIRROR_URL);

      final NodeList nodes = (NodeList) xpath.evaluate(
          "/Mirrors/Mirror", document, XPathConstants.NODESET);

      List<String> xmlMirrors = new ArrayList<String>();

      for (int i = 0; i < nodes.getLength(); i++) {
        final Element element = (Element) nodes.item(i);
        final String typeMask =
            (String) xpath.evaluate("typemask/text()", element, XPathConstants.STRING);
        final int mask = Integer.parseInt(typeMask);
        final String mirrorPath = (String) xpath.evaluate(
            "mirrorpath/text()", element, XPathConstants.STRING);
        if ((mask & MIRROR_TYPE_XML) != 0) {
          xmlMirrors.add(mirrorPath);
        }
      }

      mirror = xmlMirrors.get(random.nextInt(xmlMirrors.size()));

    } catch (Exception e) {
      mirror = MASTER_URL;
    }
    return mirror;
  }
}
