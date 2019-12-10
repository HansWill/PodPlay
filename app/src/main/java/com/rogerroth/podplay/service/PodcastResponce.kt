package com.rogerroth.podplay.service

data class PodcastResponce(val resultCount: Int, val results: List<ItunesPodcast>) {

	data class ItunesPodcast(
		val collectionCensoredName: String,
		val feedUrl: String,
		val artWorkUrl: String,
	val releaseDate: String
	)
}