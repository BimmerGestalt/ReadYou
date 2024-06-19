package io.bimmergestalt.reader.carapp.views

import android.util.Log
import de.bmw.idrive.BMWRemoting
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIActionListCallback
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIComponent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIModel
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIProperty
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState
import io.bimmergestalt.reader.GraphicsUtils
import io.bimmergestalt.reader.L
import io.bimmergestalt.reader.Utils.par
import io.bimmergestalt.reader.carapp.FeedConfig
import io.bimmergestalt.reader.carapp.FeedSelection
import io.bimmergestalt.reader.carapp.Model
import io.bimmergestalt.reader.carapp.RHMIActionAbort
import io.bimmergestalt.reader.carapp.TAG
import kotlinx.coroutines.flow.collectLatest
import me.ash.reader.domain.service.RssService

class FeedView(state: RHMIState, val rssService: RssService, val model: Model, val graphicsUtils: GraphicsUtils): OnFocusedView(state) {
	val iconSize = 32
	val feedList = state.componentsList.filterIsInstance<RHMIComponent.List>().first()
	var feedOptions = emptyList<FeedSelection>()

	fun initWidgets() {
		feedList.setProperty(RHMIProperty.PropertyId.LIST_COLUMNWIDTH, "32,${iconSize},*")
		feedList.getModel()?.value = RHMIModel.RaListModel.RHMIListConcrete(3).apply {
			addRow(arrayOf("", "", L.UNREAD))
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
			feedOptions.add(FeedSelection(L.UNREAD, null, FeedConfig.UNREAD))
			feedOptions.add(FeedSelection(L.STARRED, null, FeedConfig.STARRED))
			feedOptions.addAll(groups.map {
				FeedSelection(it.name, null, FeedConfig.GROUP(it.id))
			})
			feedOptions.add(FeedSelection(L.FEEDS, null, FeedConfig.PLACEHOLDER))
			feedOptions.addAll(feeds.map {
				FeedSelection(it.name, it.icon, FeedConfig.FEED(it.id))
			})

			feedList.getModel()?.value = object: RHMIModel.RaListModel.RHMIListAdapter<FeedSelection>(3, feedOptions) {
				override fun convertRow(index: Int, item: FeedSelection): Array<Any> {
					return if (item.feedConfig.isPlaceholder) {
						arrayOf("-", "", item.name)
					} else {
						arrayOf("", "", item.name)
					}
				}
			}
			this.feedOptions = feedOptions
			feedList.setProperty(RHMIProperty.PropertyId.LABEL_WAITINGANIMATION, false)

			// now show the icons
			feedList.getModel()?.value = RHMIModel.RaListModel.RHMIListConcrete(3).apply {
				val rows: List<Array<Any>> = feedOptions.par.map { item ->
					val heading = if (item.feedConfig.isPlaceholder) "-" else ""
					val icon = graphicsUtils.loadImageUri(item.icon, iconSize, iconSize)?.let {
						graphicsUtils.resizeDrawable(it, iconSize, iconSize)
					}?.let {
						graphicsUtils.compressBitmapJpg(it, 85)
					}?.let {
						BMWRemoting.RHMIResourceData(BMWRemoting.RHMIResourceType.IMAGEDATA, it)
					} ?: ""
					arrayOf(heading, icon, item.name)
				}
				rows.forEach {
					addRow(it)
				}
			}
		}
	}
}