#!/usr/bin/env scala
!#

import java.net._

val githubURL = "https://github.com/languages/"
val regex = "<li><a href=\"/languages/(.+)\"".r
val url = io.Source.fromURL(githubURL).mkString
for (language <- (regex findAllIn url).matchData) {
  val lang = language.subgroups mkString
  val sub = io.Source.fromURL(githubURL+lang).mkString
  for(rank <- "is the #([0-9]+)".r findAllIn sub)
    println(URLDecoder.decode(lang, "UTF-8") +" "+ rank)  
}
