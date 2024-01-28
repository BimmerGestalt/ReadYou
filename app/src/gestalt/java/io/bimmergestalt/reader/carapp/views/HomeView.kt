package io.bimmergestalt.reader.carapp.views

import android.util.Log
import androidx.paging.PagingSource
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIActionButtonCallback
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIActionListCallback
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIComponent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIModel
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIProperty
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState
import io.bimmergestalt.reader.L
import io.bimmergestalt.reader.carapp.Model
import io.bimmergestalt.reader.carapp.RHMIActionAbort
import io.bimmergestalt.reader.carapp.TAG
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.service.RssService

class HomeView(state: RHMIState, val rssService: RssService, val model: Model): OnFocusedView(state) {
	private val feedButton = state.componentsList.filterIsInstance<RHMIComponent.Button>().first()
	private val updatedLabel = state.componentsList.filterIsInstance<RHMIComponent.Label>()[0]
	private val loadingLabel = state.componentsList.filterIsInstance<RHMIComponent.Label>()[1]
	private val offlineLabel = state.componentsList.filterIsInstance<RHMIComponent.Label>()[2]
	private val entriesList = state.componentsList.filterIsInstance<RHMIComponent.List>().first()
	private val updateButton = state.optionComponentsList.filterIsInstance<RHMIComponent.Button>().first()
	private var updateTask: Job? = null

	private var data = emptyList<ArticleWithFeed>()

	fun initWidgets() {
		feedButton.getModel()?.asRaDataModel()?.value = L.UNREAD
		feedButton.setProperty(RHMIProperty.PropertyId.VISIBLE, true)

		entriesList.setProperty(RHMIProperty.PropertyId.LIST_COLUMNWIDTH, "16,*")
		entriesList.getModel()?.value = RHMIModel.RaListModel.RHMIListConcrete(2)
		entriesList.getAction()?.asRAAction()?.rhmiActionCallback = RHMIActionListCallback { i ->
//			Log.i(TAG, "User clicked entry $i")
			data.getOrNull(i) ?: throw RHMIActionAbort()
			model.articleIndex.value = i
		}

		updateButton.getAction()?.asRAAction()?.rhmiActionCallback = RHMIActionButtonCallback {
			updateTask?.cancel()
			updateTask = coroutineScope.launch { rssService.get().doSync() }
		}
	}

	fun getFeedButtonDest() = feedButton.getAction()?.asHMIAction()?.target!!
	fun getEntryListDest() = entriesList.getAction()?.asHMIAction()?.target!!

	override suspend fun onFocus() {
		model.isSyncing.collectLatest { active ->
			loadingLabel.setVisible(active)

			entriesList.setProperty(RHMIProperty.PropertyId.LABEL_WAITINGANIMATION, true)
			if (!active) {
				showFeed()
			}
		}
	}

	suspend fun showFeed() {
		val account = rssService.get()
		model.feed.collectLatest { feedSelection ->
			feedButton.getModel()?.asRaDataModel()?.value = feedSelection.name

			val fc = feedSelection.feedConfig
			val result = account.pullArticles(groupId = fc.groupId, feedId = fc.feedId, isStarred = fc.isStarred, isUnread = fc.isUnread)
				.load(PagingSource.LoadParams.Refresh(null, 50, false))
//			Log.i(TAG, "Received result $result")
			data = when (result) {
				is PagingSource.LoadResult.Page -> result.data
				else -> emptyList()
			}
			model.articles.value = data
			if (data.isNotEmpty()) {
				entriesList.getModel()?.value = object: RHMIModel.RaListModel.RHMIListAdapter<ArticleWithFeed>(2, data) {
					override fun convertRow(index: Int, item: ArticleWithFeed): Array<Any> {
						val icon = if (item.article.isUnread) "•"
						else if (item.article.isStarred) "★"
						else ""
						return arrayOf(icon, item.article.title)
					}
				}
			} else {
				entriesList.getModel()?.value = RHMIModel.RaListModel.RHMIListConcrete(2).apply {
					addRow(arrayOf("", L.EMPTY))
				}
			}
			entriesList.setEnabled(data.isNotEmpty())
			entriesList.setProperty(RHMIProperty.PropertyId.LABEL_WAITINGANIMATION, false)
		}
	}
}