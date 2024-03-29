package com.rogerroth.podplay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rogerroth.podplay.R
import com.rogerroth.podplay.util.DateUtils
import com.rogerroth.podplay.util.HtmlUtils
import com.rogerroth.podplay.viewmodel.PodcastViewModel

class EpisodeListAdapter(private var episodeViewList: List<PodcastViewModel.EpisodeViewData>?) :
	RecyclerView.Adapter<EpisodeListAdapter.ViewHolder>() {

	class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
		var episodeViewData: PodcastViewModel.EpisodeViewData? = null
		val titleTextView: TextView = v.findViewById(R.id.titleView)
		val descTextView: TextView = v.findViewById(R.id.descView)
		val durationTextView: TextView = v.findViewById(R.id.durationView)
		val releaseDateTextView: TextView = v.findViewById(R.id.releaseDateView)
	}
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeListAdapter.ViewHolder {
		return ViewHolder(LayoutInflater.from(parent.context)
			.inflate(R.layout.episode_item, parent, false))
	}
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val episodeViewList = episodeViewList ?: return
		val episodeView = episodeViewList[position]
		holder.episodeViewData = episodeView
		holder.titleTextView.text = episodeView.title
		holder.descTextView.text = HtmlUtils.htmlToSpannable(episodeView.description ?: "")
		holder.durationTextView.text = episodeView.duration
		holder.releaseDateTextView.text = episodeView.releaseDate?.let {
			DateUtils.dateToShortDate(it)
		}
	}
	override fun getItemCount(): Int {
		return episodeViewList?.size ?: 0

	}
}