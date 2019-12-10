package com.rogerroth.podplay.repository

import com.rogerroth.podplay.service.ItunesService
import com.rogerroth.podplay.service.PodcastResponce
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItunesRepo(private val itunesService: ItunesService) {
	fun searchByTerm(term: String, callBack: (List<PodcastResponce.ItunesPodcast>?) -> Unit) {
		val podcatCall = itunesService.sarchPodcastByTerm(term)
		podcatCall.enqueue(object : Callback<PodcastResponce> {
			override fun onFailure(call: Call<PodcastResponce>?, t: Throwable?) {
				callBack(null)
			}

			override fun onResponse(call: Call<PodcastResponce>?, response: Response<PodcastResponce>?) {
				val  body = response?.body()
				callBack(body?.results)
			}
		})
	}
}