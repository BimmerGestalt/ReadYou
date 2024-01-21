package io.bimmergestalt.reader.carapp.views

import android.util.Log
import androidx.core.text.HtmlCompat
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIActionButtonCallback
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIComponent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIModel
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIProperty
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState
import io.bimmergestalt.idriveconnectkit.rhmi.RequestDataCallback
import io.bimmergestalt.reader.L
import io.bimmergestalt.reader.carapp.Model
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReadView(state: RHMIState.ToolbarState, val model: Model): OnFocusedView(state) {
	private val bodyList = state.componentsList.filterIsInstance<RHMIComponent.List>()[0]
	private val image = state.componentsList.filterIsInstance<RHMIComponent.Image>()[0]
	private val readoutButton = state.toolbarComponentsList[3]
	private val previousButton = state.toolbarComponentsList[5]
	private val nextButton = state.toolbarComponentsList[6]

	private var listModel: RHMIModel.RaListModel.RHMIList = RHMIModel.RaListModel.RHMIListConcrete(1)

	fun initWidgets() {
		state.getTextModel()?.asRaDataModel()?.value = ""

		bodyList.setProperty(RHMIProperty.PropertyId.VALID, false)
		bodyList.requestDataCallback = RequestDataCallback { startIndex, numRows -> showList(startIndex, numRows) }

		previousButton.getAction()?.asRAAction()?.rhmiActionCallback = RHMIActionButtonCallback {
			val index =  model.articleIndex.value
			if (index > 0) {
				model.articleIndex.value = index-1
			}
		}
		nextButton.getAction()?.asRAAction()?.rhmiActionCallback = RHMIActionButtonCallback {
			val index = model.articleIndex.value
			if (index >= 0 && index < model.articles.value.size - 1) {
				model.articleIndex.value = index+1
			}
		}
	}

	fun getReadoutDest() = readoutButton.getAction()?.asHMIAction()?.target!!

	override suspend fun onFocus() {
		coroutineScope.launch {
			model.canSkipPrevious.collectLatest {
				previousButton.setProperty(RHMIProperty.PropertyId.ENABLED, it)
			}
		}
		coroutineScope.launch {
			model.canSkipNext.collectLatest {
				nextButton.setProperty(RHMIProperty.PropertyId.ENABLED, it)
			}
		}

		bodyList.setProperty(RHMIProperty.PropertyId.LABEL_WAITINGANIMATION, true)
		model.article.collectLatest { article ->
			if (article == null) {
				return@collectLatest
			}
			state.getTextModel()?.asRaDataModel()?.value = article.feed.name

			val htmlContents = (article.article.fullContent ?: "")
				.replace(Regex("<h1.*?<.*?>"), "")
			val contents = HtmlCompat.fromHtml(htmlContents,
				HtmlCompat.FROM_HTML_MODE_COMPACT
			).toString()
				.lines()
			listModel = RHMIModel.RaListModel.RHMIListConcrete(1).also {
				it.addRow(arrayOf(article.article.title))
				it.addRow(arrayOf(L.mediumDateFormat.format(article.article.date) + " " + L.timeFormat.format(article.article.date)))
				contents.forEach { line ->
					it.addRow(arrayOf(line))
				}
			}
			showList(0, 5)
			bodyList.setProperty(RHMIProperty.PropertyId.LABEL_WAITINGANIMATION, false)
		}
	}

	private fun showList(start: Int, count: Int) {
		if (start >= 0) {
			bodyList.getModel()?.setValue(listModel, start, count, listModel.height)
		}
	}

	override fun onBlur() {
		bodyList.getModel()?.value = RHMIModel.RaListModel.RHMIListConcrete(1)
	}
}