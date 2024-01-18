package io.bimmergestalt.reader.carapp.views

import android.util.Log
import androidx.core.text.HtmlCompat
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIActionButtonCallback
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIComponent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIModel
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIProperty
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState
import io.bimmergestalt.reader.L
import io.bimmergestalt.reader.carapp.Model
import io.bimmergestalt.reader.carapp.TAG
import kotlinx.coroutines.flow.collectLatest

class ReadView(state: RHMIState.ToolbarState, val model: Model): OnFocusedView(state) {
	private val bodyList = state.componentsList.filterIsInstance<RHMIComponent.List>()[0]
	private val image = state.componentsList.filterIsInstance<RHMIComponent.Image>()[0]
	private val readoutButton = state.toolbarComponentsList[3]
	private val previousButton = state.toolbarComponentsList[5]
	private val nextButton = state.toolbarComponentsList[6]

	fun initWidgets() {
		state.getTextModel()?.asRaDataModel()?.value = ""

		previousButton.getAction()?.asRAAction()?.rhmiActionCallback = RHMIActionButtonCallback {
			val index =  model.articleIndex.value
			if (index > 0) {
				model.articleIndex.value = index-1
				model.article.value = model.articles[index-1]
			}
		}
		nextButton.getAction()?.asRAAction()?.rhmiActionCallback = RHMIActionButtonCallback {
			val index = model.articleIndex.value
			if (index >= 0 && index < model.articles.size - 1) {
				model.articleIndex.value = index+1
				model.article.value = model.articles[index+1]
			}
		}
	}

	fun getReadoutDest() = readoutButton.getAction()?.asHMIAction()?.target!!

	override suspend fun onFocus() {
		bodyList.setProperty(RHMIProperty.PropertyId.LABEL_WAITINGANIMATION, true)
		model.articleIndex.collectLatest { index ->
			previousButton.setProperty(RHMIProperty.PropertyId.ENABLED, index > 0)
			nextButton.setProperty(RHMIProperty.PropertyId.ENABLED, index < model.articles.size - 1)
			val article = model.articles.getOrNull(index)
			if (article == null) {
				Log.w(TAG, "Could not find article #$index/${model.articles.size} in feed view ${model.feed.value.name}")
				return@collectLatest
			} else {
				Log.i(TAG, "Found article $index ${article.article.title}")
			}
			state.getTextModel()?.asRaDataModel()?.value = article.feed.name

			val htmlContents = (article.article.fullContent ?: "")
				.replace(Regex("<h1.*?<.*?>"), "")
			val contents = HtmlCompat.fromHtml(htmlContents,
				HtmlCompat.FROM_HTML_MODE_COMPACT
			).toString()
			bodyList.getModel()?.value = RHMIModel.RaListModel.RHMIListConcrete(1).also {
				it.addRow(arrayOf(article.article.title))
				it.addRow(arrayOf(L.mediumDateFormat.format(article.article.date) + " " + L.timeFormat.format(article.article.date)))
				it.addRow(arrayOf(contents))
			}
			bodyList.setProperty(RHMIProperty.PropertyId.LABEL_WAITINGANIMATION, false)
		}
	}

	override fun onBlur() {
		bodyList.getModel()?.value = RHMIModel.RaListModel.RHMIListConcrete(1)
	}
}