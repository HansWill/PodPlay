package com.rogerroth.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.rogerroth.podplay.R
import com.rogerroth.podplay.adapter.PodcastListAdapter
import com.rogerroth.podplay.repository.ItunesRepo
import com.rogerroth.podplay.repository.PodcastRepo
import com.rogerroth.podplay.service.FeedService
import com.rogerroth.podplay.service.ItunesService
import com.rogerroth.podplay.service.RssFeedService
import com.rogerroth.podplay.viewmodel.PodcastViewModel
import com.rogerroth.podplay.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.activity_podcast.*

class PodcastActivity : AppCompatActivity(), PodcastListAdapter.PodcastListAdapterListener {

	private lateinit var searchViewModel: SearchViewModel
	private lateinit var podcastListAdapter: PodcastListAdapter
	private lateinit var searchMenuItem: MenuItem
	private lateinit var podcastViewModel: PodcastViewModel

	private val TAG = javaClass.simpleName

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_podcast)

		setupToolbar()
		setupViewModels()
		updateControls()
		handleIntent(intent)
		addBackStacklistener()

	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val inflater = menuInflater
		inflater.inflate(R.menu.menu_search, menu)
		searchMenuItem = menu.findItem(R.id.search_item)
		val searchView = searchMenuItem.actionView as SearchView
		val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
		searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

		if (supportFragmentManager.backStackEntryCount > 0) {
			podcastRecyclerView.visibility = View.INVISIBLE
		}

		if (podcastRecyclerView.visibility == View.INVISIBLE) {
			searchMenuItem.isVisible = false

		}

		return true
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		setIntent(intent)
		handleIntent(intent)
	}

	override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {
		val feedUrl = podcastSummaryViewData.feedUrl ?: return
		showProgressBar()
		podcastViewModel.getPodcast(podcastSummaryViewData) {
			hideProgressBar()
			if (it != null) {
				showDetailsFragment()
			} else {
				showError("Error loading feed $feedUrl")
			}
		}
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

		podcastViewModel = ViewModelProviders.of(this).get(PodcastViewModel::class.java)
		val rssService = FeedService.instance
		podcastViewModel.podcastRepo = PodcastRepo(rssService as RssFeedService)
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

	private fun createPodcastDetailsFragment(): PodcastDetailsFragment {
		var podcastDetailsFragment = supportFragmentManager
			.findFragmentByTag(TAG_DETAILS_FRAGMENT) as PodcastDetailsFragment?

		if (podcastDetailsFragment == null) {
			podcastDetailsFragment = PodcastDetailsFragment.newInstance()
		}
		return podcastDetailsFragment
	}

	private fun showDetailsFragment() {

		val podcastDetailsFragment = createPodcastDetailsFragment()

		supportFragmentManager.beginTransaction().add(R.id.podcastDetailsContainer,
			podcastDetailsFragment, TAG_DETAILS_FRAGMENT)
			.addToBackStack("DetailsFragment").commit()

		podcastRecyclerView.visibility = View.INVISIBLE
		searchMenuItem.isVisible = false
	}

	private fun showError(message: String) {
		AlertDialog.Builder(this)
			.setMessage(message)
			.setPositiveButton(getString(R.string.ok_button), null)
			.create()
			.show()
	}

	private fun addBackStacklistener() {
		supportFragmentManager.addOnBackStackChangedListener {
			if (supportFragmentManager.backStackEntryCount == 0) {
				podcastRecyclerView.visibility = View.VISIBLE
			}
		}
	}

	companion object {
		private val TAG_DETAILS_FRAGMENT = "DetailsFragment"
	}


}
