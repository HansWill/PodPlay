package com.rogerroth.podplay.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

	fun jsonDateToShortDate(jsonDate: String?): String {

		if (jsonDate == null) {
			return "-"
		}
		val inFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
		val date = inFormat.parse(jsonDate)
		val outputFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
		return outputFormat.format(date)
	}

	fun xmlDateToDate(date: String?): Date {
		val date = date ?: return Date()
		val inFormat = SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss z")
		return inFormat.parse(date)
	}

	fun dateToShortDate(date: Date): String {
		val outputFormat = DateFormat.getDateInstance(
			DateFormat.SHORT, Locale.getDefault())
		return outputFormat.format(date)
	}
}