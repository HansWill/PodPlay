package com.rogerroth.podplay.repository

import com.rogerroth.podplay.model.Episode
import com.rogerroth.podplay.model.Podcast
import com.rogerroth.podplay.service.RssFeedResponse
import com.rogerroth.podplay.service.RssFeedService
import com.rogerroth.podplay.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastRepo(private var feedService: RssFeedService) {
	fun getPodcast(feedUrl: String, callback: (Podcast?) -> Unit) {
		feedService.getFeed(feedUrl) { feedResponse ->
			var podcast: Podcast? = null
			if (feedResponse != null) {
				podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
			}
			GlobalScope.launch(Dispatchers.Main) {
				callback(podcast)
			}
		}
	}

	private fun rssItemsToEpisodes(episodeResponses: List<RssFeedResponse.EpisodesResponse>): List<Episode> {
		return episodeResponses.map {
			Episode(
				it.guid ?: "",
				it.title ?: "",
				it.description ?: "",
				it.url ?: "",
				it.type ?: "",
				DateUtils.xmlDateToDate(it.pubDate),
				it.duration ?: ""
			)
		}
	}

	private fun rssResponseToPodcast(feedUrl: String, imageUrl: String, rssResponse: RssFeedResponse): Podcast? {
		val items = rssResponse.episodes ?: return null
		val description = if (rssResponse.description == "")
			rssResponse.summary else rssResponse.description
		return Podcast(feedUrl, rssResponse.title, description, imageUrl,
			rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
	}
}