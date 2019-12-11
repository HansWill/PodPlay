package com.rogerroth.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.rogerroth.podplay.R
import com.rogerroth.podplay.adapter.PodcastListAdapter
import com.rogerroth.podplay.repository.ItunesRepo
import com.rogerroth.podplay.service.ItunesService
import com.rogerroth.podplay.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.activity_podcast.*

class PodcastActivity : AppCompatActivity(), PodcastListAdapter.PodcastListAdapterListener {

	private lateinit var searchViewModel: SearchViewModel
	private lateinit var podcastListAdapter: PodcastListAdapter

	private val TAG = javaClass.simpleName

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_podcast)

		setupToolbar()
		setupViewModels()
		updateControls()
		handleIntent(intent)

		val TAG = javaClass.simpleName
		val itunesService = ItunesService.instance
		val itunesRepo = ItunesRepo(itunesService)

		itunesRepo.searchByTerm("Android Developer") {
			Log.i(TAG, "Results = $it")
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val inflater = menuInflater
		inflater.inflate(R.menu.menu_search, menu)
		val  searchMenuItem = menu.findItem(R.id.search_item)
		val searchView = searchMenuItem?.actionView as SearchView
		val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
		searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

		return true
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		setIntent(intent)
		handleIntent(intent)
	}

	override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {
		// Not implemented yet
	}

	private fun showProgressBar() {
		progressBar.visibility = View.VISIBLE
	}

	private fun hideProgressBar() {
		progressBar.visibility = View.INVISIBLE
	}

	private fun setupViewModels() {
		val service = ItunesService.instance
		searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
		searchViewModel.itunesRepo = ItunesRepo(service)
	}

	private fun updateControls() {
		podcastRecyclerView.setHasFixedSize(true)

		val layoutManager = LinearLayoutManager(this)
		podcastRecyclerView.layoutManager = layoutManager

		val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(podcastRecyclerView.context, layoutManager.orientation)
		podcastRecyclerView.addItemDecoration(dividerItemDecoration)

		podcastListAdapter = PodcastListAdapter(null, this, this)
		podcastRecyclerView.adapter = podcastListAdapter
	}

	private fun setupToolbar() {
		setSupportActionBar(toolbar)
	}

	private fun performSearch(term: String) {
		showProgressBar()
		searchViewModel.searchPodcasts(term) { results ->
			hideProgressBar()
			toolbar.title = getString(R.string.search_results)
			podcastListAdapter.setSearchData(results)
		}
	}

	private fun handleIntent(intent: Intent) {
		if (Intent.ACTION_SEARCH == intent.action) {
			val query = intent.getStringExtra(SearchManager.QUERY)
			performSearch(query)
		}
	}


}
