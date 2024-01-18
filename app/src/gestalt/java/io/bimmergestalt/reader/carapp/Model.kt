package io.bimmergestalt.reader.carapp

import kotlinx.coroutines.flow.MutableStateFlow
import me.ash.reader.domain.model.article.ArticleWithFeed

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

class Model {
	var feed = MutableStateFlow(FeedSelection("Unread", FeedConfig.UNREAD))
	var articles = emptyList<ArticleWithFeed>()
	var article = MutableStateFlow<ArticleWithFeed?>(null)
	var articleIndex = MutableStateFlow(-1)
}