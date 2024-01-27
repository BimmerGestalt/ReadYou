package io.bimmergestalt.reader.carapp.views

import io.bimmergestalt.idriveconnectkit.rhmi.RHMIActionListCallback
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIComponent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIModel
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIProperty
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState
import io.bimmergestalt.reader.L
import io.bimmergestalt.reader.carapp.FeedConfig
import io.bimmergestalt.reader.carapp.FeedSelection
import io.bimmergestalt.reader.carapp.Model
import io.bimmergestalt.reader.carapp.RHMIActionAbort
import kotlinx.coroutines.flow.collectLatest
import me.ash.reader.domain.service.RssService

class FeedView(state: RHMIState, val rssService: RssService, val model: Model): OnFocusedView(state) {
	val feedList = state.componentsList.filterIsInstance<RHMIComponent.List>().first()
	var feedOptions = emptyList<FeedSelection>()

	fun initWidgets() {
		feedList.setProperty(RHMIProperty.PropertyId.LIST_COLUMNWIDTH, "32,*")
		feedList.getModel()?.value = RHMIModel.RaListModel.RHMIListConcrete(2).apply {
			addRow(arrayOf("", L.UNREAD))
		}
		feedList.getAction()?.asRAAction()?.rhmiActionCallback = RHMIActionListCallback { i ->
			val option = feedOptions.getOrNull(i) ?: throw RHMIActionAbort()
			if (option.feedConfig.isPlaceholder) {
				throw RHMIActionAbort()
			}
			model.feed.value = option
		}
	}

	override suspend fun onFocus() {
		val account = rssService.get()

		feedList.setProperty(RHMIProperty.PropertyId.LABEL_WAITINGANIMATION, true)
		account.pullFeeds().collectLatest { feedWithGroups ->
			val groups = feedWithGroups
				.map { it.group }
				.sortedBy { it.name }
			val feeds = feedWithGroups
				.flatMap { it.feeds }
				.sortedBy { it.name }

			val feedOptions = ArrayList<FeedSelection>(groups.size + feeds.size + 3)
			feedOptions.add(FeedSelection(L.UNREAD, FeedConfig.UNREAD))
			feedOptions.add(FeedSelection(L.STARRED, FeedConfig.STARRED))
			feedOptions.addAll(groups.map {
				FeedSelection(it.name, FeedConfig.GROUP(it.id))
			})
			feedOptions.add(FeedSelection(L.FEEDS, FeedConfig.PLACEHOLDER))
			feedOptions.addAll(feeds.map {
				// TODO parse the url from it.icon like FeedIcon, which might be a base64 data
				FeedSelection(it.name, FeedConfig.FEED(it.id))
			})

			feedList.getModel()?.value = object: RHMIModel.RaListModel.RHMIListAdapter<FeedSelection>(2, feedOptions) {
				override fun convertRow(index: Int, item: FeedSelection): Array<Any> {
					return if (item.feedConfig.isPlaceholder) {
						arrayOf("-", item.name)
					} else {
						arrayOf("", item.name)
					}
				}
			}
			this.feedOptions = feedOptions
			feedList.setProperty(RHMIProperty.PropertyId.LABEL_WAITINGANIMATION, false)
		}
	}
}