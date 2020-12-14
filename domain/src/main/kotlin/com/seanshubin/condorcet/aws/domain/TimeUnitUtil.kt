package com.seanshubin.condorcet.aws.domain

object TimeUnitUtil {
  fun minutesToSeconds(minutes: Int): Int = minutes * 60
  fun hoursToMinutes(hours: Int): Int = hours * 60
  fun hoursToSeconds(hours: Int): Int = minutesToSeconds(hoursToMinutes(hours))
}