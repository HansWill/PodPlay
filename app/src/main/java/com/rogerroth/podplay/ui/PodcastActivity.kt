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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Lifetime
import com.firebase.jobdispatcher.Trigger
import com.rogerroth.podplay.R
import com.rogerroth.podplay.adapter.PodcastListAdapter
import com.rogerroth.podplay.db.PodplayDatabase
import com.rogerroth.podplay.repository.ItunesRepo
import com.rogerroth.podplay.repository.PodcastRepo
import com.rogerroth.podplay.service.EpisodeUpdateService
import com.rogerroth.podplay.service.FeedService
import com.rogerroth.podplay.service.ItunesService
import com.rogerroth.podplay.service.RssFeedService
import com.rogerroth.podplay.viewmodel.PodcastViewModel
import com.rogerroth.podplay.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.activity_podcast.*

class PodcastActivity : AppCompatActivity(), PodcastListAdapter.PodcastListAdapterListener, PodcastDetailsFragment.OnPodcastDetailsListener {

	private lateinit var searchViewModel: SearchViewModel
	private lateinit var podcastListAdapter: PodcastListAdapter
	private lateinit var searchMenuItem: MenuItem
	private lateinit var podcastViewModel: PodcastViewModel

	private val TAG_EPISODE_UPDATE_JOB = "com.rogerroth.podplay.episodes"
	private val TAG = javaClass.simpleName

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_podcast)

		setupToolbar()
		setupViewModels()
		updateControls()
		handleIntent(intent)
		addBackStacklistener()
		setupPodcastListView()
		scheduleJobs()

	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val inflater = menuInflater
		inflater.inflate(R.menu.menu_search, menu)
		searchMenuItem = menu.findItem(R.id.search_item)
		searchMenuItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
			override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
				return true
			}

			override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
				showSubscribedPodcasts()
				return true
			}
		} )
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

	override fun onSubscribe() {
		podcastViewModel.saveActivePodcast()
		supportFragmentManager.popBackStack()
	}

	override fun onUnsubscribe() {
		podcastViewModel.deleteActivePodcast()
		supportFragmentManager.popBackStack()
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
		val db = PodplayDatabase.getInstance(this)
		val podcastDao = db.podcastDao()
		podcastViewModel.podcastRepo = PodcastRepo(rssService as RssFeedService, podcastDao)
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
			val podcastFeedUrl = intent
				.getStringExtra(EpisodeUpdateService.EXTRA_FEED_URL)
			if (podcastFeedUrl != null) {
				podcastViewModel.setActivePodcast(podcastFeedUrl) {
					it?.let { podcastSummaryView -> onShowDetails(podcastSummaryView) }
				}
			}

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

	private fun showSubscribedPodcasts() {
		val podcasts = podcastViewModel.getPodcasts()?.value
		if (podcasts != null) {
			toolbar.title = getString(R.string.subscribed_podcasts)
			podcastListAdapter.setSearchData(podcasts)
		}
	}

	private fun setupPodcastListView() {
		podcastViewModel.getPodcasts()?.observe(this, Observer {
			if (it != null) {
				showSubscribedPodcasts()
			}
		})
	}

	private fun scheduleJobs()
	{

		val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
		val oneHourInSeconds = 60*60
		val tenMinutesInSeconds = 60*10
		val episodeUpdateJob = dispatcher.newJobBuilder()
			.setService(EpisodeUpdateService::class.java)
			.setTag(TAG_EPISODE_UPDATE_JOB)
			.setRecurring(true)
			.setTrigger(Trigger.executionWindow(oneHourInSeconds, (oneHourInSeconds + tenMinutesInSeconds)))
			.setLifetime(Lifetime.FOREVER)
			.setConstraints(Constraint.ON_UNMETERED_NETWORK, Constraint.DEVICE_CHARGING)
			.build()
		dispatcher.mustSchedule(episodeUpdateJob)
	}

	companion object {
		private const val TAG_DETAILS_FRAGMENT = "DetailsFragment"
	}


}
