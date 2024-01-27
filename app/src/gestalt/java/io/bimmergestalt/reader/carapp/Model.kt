package io.bimmergestalt.reader.carapp

import androidx.lifecycle.asFlow
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.bimmergestalt.reader.L
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.service.SyncWorker

data class FeedConfig(val groupId: String?, val feedId: String?,
                      val isStarred: Boolean, val isUnread: Boolean) {
	companion object {
		val STARRED = FeedConfig(null, null, isStarred = true, isUnread = false)
		val UNREAD = FeedConfig(null, null, isStarred = false, isUnread = true)
		val PLACEHOLDER = FeedConfig(null, null, isStarred = false, isUnread = false)

		fun FEED(feedId: String) = FeedConfig(null, feedId, isStarred = false, isUnread = false)
		fun GROUP(groupId: String) = FeedConfig(groupId, null, isStarred = false, isUnread = false)
	}
	val isPlaceholder = (groupId == null && feedId == null && !isStarred && !isUnread)
}
class FeedSelection(val name: String, val feedConfig: FeedConfig)

class Model(workManager: WorkManager) {
	val isSyncing = workManager.getWorkInfosByTagLiveData(SyncWorker.WORK_NAME)
		.asFlow().map { it.any { workInfo ->
			workInfo.state == WorkInfo.State.RUNNING
		} }
	var feed = MutableStateFlow(FeedSelection(L.UNREAD, FeedConfig.UNREAD))
	var articles = MutableStateFlow(emptyList<ArticleWithFeed>())
	var articleIndex = MutableStateFlow(-1)
	val article = articles.combine(articleIndex) { articles, index ->
		articles.getOrNull(index)
	}

	val canSkipPrevious = articles.combine(articleIndex) { articles, index ->
		index > 0 && articles.isNotEmpty()
	}
	val canSkipNext = articles.combine(articleIndex) { articles, index ->
		index < articles.size - 1
	}
}