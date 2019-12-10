package com.rogerroth.podplay.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.rogerroth.podplay.R
import com.rogerroth.podplay.repository.ItunesRepo
import com.rogerroth.podplay.service.ItunesService

class PodcastActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_podcast)

		val TAG = javaClass.simpleName
		val itunesService = ItunesService.instance
		val itunesRepo = ItunesRepo(itunesService)

		itunesRepo.searchByTerm("Android Developer") {
			Log.i(TAG, "Results = $it")
		}
	}
}
