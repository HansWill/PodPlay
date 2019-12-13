package com.rogerroth.podplay.ui

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.rogerroth.podplay.R
import com.rogerroth.podplay.adapter.EpisodeListAdapter
import com.rogerroth.podplay.viewmodel.PodcastViewModel
import kotlinx.android.synthetic.main.fragment_podcast_details.*
import java.lang.RuntimeException

class PodcastDetailsFragment : Fragment() {

	private lateinit var podcastViewModel: PodcastViewModel
	private lateinit var episodeListAdapter: EpisodeListAdapter
	private var listener: OnPodcastDetailsListener? = null
	private var menuItem: MenuItem? = null

	companion object {
		fun newInstance(): PodcastDetailsFragment {
			return PodcastDetailsFragment()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
		setupViewModel()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_podcast_details, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		updateControls()
		setupControls()
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater?.inflate(R.menu.menu_details, menu)
		menuItem = menu?.findItem(R.id.menu_feed_action)
		updateItem()
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is OnPodcastDetailsListener) {
			listener = context
		} else {
			throw RuntimeException(context!!.toString() + "must implement OnPodcastDetailsListener")
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.menu_feed_action -> {
				podcastViewModel.activePodcastViewData?.feedUrl?.let {
					if (podcastViewModel.activePodcastViewData?.subscribed!!) {
						listener?.onUnsubscribe()
					} else {
						listener?.onSubscribe()
					}
				}
				return true
			} else -> return super.onOptionsItemSelected(item)
		}
	}

	private fun setupViewModel() {
		activity?.let { activity ->
			podcastViewModel = ViewModelProviders.of(activity)
				.get(PodcastViewModel::class.java)
		}
	}

	private fun updateControls() {
		val viewData = podcastViewModel.activePodcastViewData ?: return
		feedTitleTextView.text = viewData.feedTitle
		feedDescTextView.text = viewData.feedDesc
		activity?.let { activity ->
			Glide.with(activity).load(viewData.imageUrl)
				.into(feedImageView)
		}
	}

	private fun setupControls() {
		feedDescTextView.movementMethod = ScrollingMovementMethod()
		episodeRecyclerView.setHasFixedSize(true)
		val layoutManager = LinearLayoutManager(activity)
		episodeRecyclerView.layoutManager = layoutManager
		val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(episodeRecyclerView.context, layoutManager.orientation)
		episodeRecyclerView.addItemDecoration(dividerItemDecoration)

		episodeListAdapter = EpisodeListAdapter(podcastViewModel.activePodcastViewData?.episodes)
		episodeRecyclerView.adapter = episodeListAdapter
	}

	private fun updateItem() {
		val viewData = podcastViewModel.activePodcastViewData ?: return
		menuItem?.title = if (viewData.subscribed)
			getString(R.string.unsubscribe) else getString(R.string.subscribe)
	}

	interface OnPodcastDetailsListener {
		fun onSubscribe()
		fun onUnsubscribe()
	}
}