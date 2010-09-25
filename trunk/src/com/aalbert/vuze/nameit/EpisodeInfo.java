// Copyright 2010 Google. All Rights Reserved.
package com.aalbert.vuze.nameit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sun.security.x509.AlgorithmId;

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

  private static int MIRROR_TYPE_XML = 1;
  private static int MIRROR_TYPE_ZIP = 4;

  private static final XPath xpath = XPathFactory.newInstance().newXPath();
  private static final Random random = new Random(new Date().getTime());

  private static Map<String, String> fixShowNames = new HashMap<String, String>();

  private String showName;
  private String episodeName;

  static {
    fixShowNames.put("Tosh 0", "Tosh.0");
  }

  public EpisodeInfo(String showName, int seasonNum, int episodeNum) {
    String fixed = fixShowNames.get(showName);
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
        // TODO(aalbert): log
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
          return;
        }
      }

      final String seriesId = series.getElementsByTagName("seriesid").item(0).getTextContent();

      uri = mirror + "/api/B007F67F2D4B5FF3/series/" + seriesId + "/all";
      document = documentBuilder.parse(uri);
      final String xpath =
          String.format("/Data/Episode[SeasonNumber=%d and EpisodeNumber=%d]/EpisodeName",
          seasonNum, episodeNum);
      nodes = (NodeList) EpisodeInfo.xpath.evaluate(xpath, document, XPathConstants.NODESET);

      if (nodes.getLength() != 1) {
        // TODO(aalbert): log
        return;
      }

      episodeName = nodes.item(0).getTextContent();
    } catch (Exception e) {
      System.out.println(e);
      // TODO(aalbert): log
    }
  }

  public String getShowName() {
    return showName;
  }

  public String getEpisodeName() {
    return episodeName;
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
