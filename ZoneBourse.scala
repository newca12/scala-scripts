#!/usr/bin/env scalas

/***
 scalaVersion := "2.11.6"

 libraryDependencies ++= Seq(
 "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.10",
 "org.apache.commons" % "commons-lang3" % "3.3.2"
 )
*/

import org.htmlcleaner.HtmlCleaner
import java.net.URL
import org.apache.commons.lang3.StringEscapeUtils
import org.htmlcleaner.TagNode

  val stocks = List("AIRBUS-GROUP-4637", "ALSTOM-4607", "DISTRIBUIDORA-INTER-DE-AL-8322842", "GENFIT-16311755", "INNATE-PHARMA-35620",
    "SOCIETE-GENERALE-4702", "STMICROELECTRONICS-4710", "TECHNIP-4712")

  def priceTargetRec(zoneBourseId: String): List[Double] = {
    val url = s"http://www.zonebourse.com/${zoneBourseId}/consensus/"
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties
    val rootNode = cleaner.clean(new URL(url))
    val elements = rootNode.getElementsByName("table", true)
    def rec(l: List[TagNode], keepNext: Boolean, acc: List[Double]): List[Double] = {
      if (l.isEmpty) acc
      else if (acc.length == 3) return acc
      else if (keepNext == true) {
        val content = l.head.getText.toString
        if (content.contains("%")) {
          val value = content.filterNot { "%" contains _ }.replace(",", ".")
          val n = value.toDouble
          rec(l.tail, false, n :: acc)
        } else rec(l.tail, false, acc)
      } else {
        val classType = l.head.getAttributeByName("class")
        if (classType != null && classType.equalsIgnoreCase("RC_tdL")) rec(l.tail, true, acc)
        else rec(l.tail, false, acc)
      }
    }

    for (elem ← elements) {
      val res = elem.getAllElements(true).toList
      val res2 = rec(res, false, Nil)
      if (!res2.isEmpty) return res2

    }
    //Should be unreachable
    Nil
  }

  def priceTarget(zoneBourseId: String): List[Double] = {
    val url = s"http://www.zonebourse.com/${zoneBourseId}/consensus/"
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties
    val rootNode = cleaner.clean(new URL(url))
    val elements = rootNode.getElementsByName("table", true)
    var r: List[Double] = Nil
    var keepNext = false
    for (elem ← elements) {
      val res = elem.getAllElements(true)
      for (i <- res) {
        if (keepNext == true) {
          keepNext = false
          val content = i.getText.toString
          if (content.contains("%")) {
            val value = content.filterNot { "%" contains _ }.replace(",", ".")
            val n = value.toDouble
            r = n :: r
            if (r.size == 3) return r.reverse
          }
        }
        val classType = i.getAttributeByName("class")
        if (classType != null && classType.equalsIgnoreCase("RC_tdL")) {
          keepNext = true
        }
      }
    }
    r
  }

  stocks.foreach { s => println(s); println(priceTargetRec(s)) }
