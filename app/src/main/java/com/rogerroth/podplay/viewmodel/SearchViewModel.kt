package com.rogerroth.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rogerroth.podplay.repository.ItunesRepo
import com.rogerroth.podplay.service.PodcastResponce
import com.rogerroth.podplay.util.DateUtils


class SearchViewModel(application: Application) : AndroidViewModel(application) {

	var itunesRepo: ItunesRepo? = null

	data class PodcastSummaryViewData(
		var name: String? = "",
		var lastUpdated: String? = "",
		var imageUrl: String? = "",
		var feedUrl: String? = ""
	)

	private fun itunesPodcastToPodcastSummaryView(itunesPodcast: PodcastResponce.ItunesPodcast): PodcastSummaryViewData {
		return PodcastSummaryViewData(
			itunesPodcast.collectionCensoredName,
			DateUtils.jsonDateToShortDate(itunesPodcast.releaseDate),
			itunesPodcast.artWorkUrl30,
			itunesPodcast.feedUrl
		)
	}

	fun searchPodcasts(term: String, callback: (List<PodcastSummaryViewData>) -> Unit) {
		itunesRepo?.searchByTerm(term) { results ->
			if (results == null) {
				callback(emptyList())
			} else {
				val searchViews = results.map { podcast ->
					itunesPodcastToPodcastSummaryView(podcast)
				}
				callback(searchViews)
			}
		}
	}
}