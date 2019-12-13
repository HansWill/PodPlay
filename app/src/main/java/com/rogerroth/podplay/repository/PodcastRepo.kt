package com.rogerroth.podplay.repository

import androidx.lifecycle.LiveData
import com.rogerroth.podplay.db.PodcastDao
import com.rogerroth.podplay.model.Episode
import com.rogerroth.podplay.model.Podcast
import com.rogerroth.podplay.service.RssFeedResponse
import com.rogerroth.podplay.service.RssFeedService
import com.rogerroth.podplay.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastRepo(private var feedService: RssFeedService, private var podcastDao: PodcastDao) {
	fun getPodcast(feedUrl: String, callback: (Podcast?) -> Unit) {
		GlobalScope.launch {
			val podcast = podcastDao.loadPodcast(feedUrl)
			if (podcast != null) {
				podcast.id?.let {
					podcast.episodes = podcastDao.loadEpisodes(it)
					GlobalScope.launch(Dispatchers.Main) {
						callback(podcast)
					}
				}
			} else {

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
		}
	}



	private fun rssItemsToEpisodes(episodeResponses: List<RssFeedResponse.EpisodesResponse>): List<Episode> {
		return episodeResponses.map {
			Episode(
				it.guid ?: "",
				null,
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
		return Podcast(null, feedUrl, rssResponse.title, description, imageUrl,
			rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
	}

	fun save(podcast: Podcast) {
		GlobalScope.launch {
			val podcastId = podcastDao.insertPodcast(podcast)
			for (episode in podcast.episodes) {
				episode.podcastId = podcastId
				podcastDao.insertEpisode(episode)
			}
		}
	}

	fun delete(podcast: Podcast) {
		GlobalScope.launch {
			podcastDao.deletePodcast(podcast)
		}
	}

	fun getAll(): LiveData<List<Podcast>> {
		return podcastDao.loadPodcasts()
	}
}