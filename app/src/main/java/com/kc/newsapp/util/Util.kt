package com.kc.newsapp.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by kaichang on 8/7/18.
 */
object Util {
    fun formatTimestamp(inputDate: String, sdf: SimpleDateFormat, timeZone: TimeZone): String {
        sdf.timeZone = timeZone
        return formatDuration(dateDiff(sdf.parse(inputDate), Date(), timeZone))
    }

    fun dateDiff(input: Date, now: Date, timeZone: TimeZone): Long {
        val c1 = Calendar.getInstance(timeZone).apply { time = input }
        val c2 = Calendar.getInstance(timeZone).apply { time = now }
        return c2.timeInMillis - c1.timeInMillis
    }

    fun formatDuration(diff: Long): String {
        return when {
            (diff / 86400000 > 0) -> "${diff / 86400000}d ago"
            (diff / 3600000 > 0) -> "${diff / 3600000}h ago"
            (diff / 60000 > 0) -> "${diff / 60000}m ago"
            else -> "${diff / 1000}s ago"
        }
    }

    fun log(msg: String) = println("Kai: [${Thread.currentThread().name}] $msg")
}