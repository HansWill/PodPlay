package com.rogerroth.podplay.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.rogerroth.podplay.R
import com.rogerroth.podplay.viewmodel.PodcastViewModel
import kotlinx.android.synthetic.main.fragment_podcast_details.*

class PodcastDetailsFragment : Fragment() {

	private lateinit var podcastViewModel: PodcastViewModel

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
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater?.inflate(R.menu.menu_details, menu)
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
}