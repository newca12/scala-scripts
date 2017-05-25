#!/usr/bin/env scalas

/***
 scalaVersion := "2.12.2"

 libraryDependencies ++= Seq(
 "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.21",
 "org.apache.commons" % "commons-lang3" % "3.5"
 )
*/

import org.htmlcleaner.HtmlCleaner
import java.net.URL
import org.apache.commons.lang3.StringEscapeUtils
import org.htmlcleaner.TagNode

  val stocks = List("DISTRIBUIDORA-INTER-DE-AL-8322842", "DBV-TECHNOLOGIES-10189744", "EDF-4998", "GENFIT-16311755", "INNATE-PHARMA-35620",
                    "NICOX-25281955", "PEUGEOT-4682", "ORANGE-SA-4649", "SOITEC-4695", "TECHNICOLOR-6411898")

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
      if (res2.nonEmpty) return res2

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
