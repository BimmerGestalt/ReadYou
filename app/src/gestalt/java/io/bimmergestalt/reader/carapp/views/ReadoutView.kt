package io.bimmergestalt.reader.carapp.views

import io.bimmergestalt.idriveconnectkit.rhmi.RHMIActionButtonCallback
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIComponent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIProperty
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState
import io.bimmergestalt.idriveconnectkit.rhmi.VisibleCallback
import io.bimmergestalt.reader.Utils
import io.bimmergestalt.reader.carapp.CarAppSharedAssetResources
import io.bimmergestalt.reader.carapp.Model
import io.bimmergestalt.reader.carapp.ReadoutController
import io.bimmergestalt.reader.carapp.ReadoutState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReadoutView(state: RHMIState.ToolbarState, val controller: ReadoutController, val model: Model): OnFocusedView(state) {
	val globalCoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

	val debugVisible = false

	val curLineLabel = state.componentsList.filterIsInstance<RHMIComponent.Label>()[0]
	val hidden1Label = state.componentsList.filterIsInstance<RHMIComponent.Label>()[1]
	val hidden2Label = state.componentsList.filterIsInstance<RHMIComponent.Label>()[2]
	val speedlockLabel = state.componentsList.filterIsInstance<RHMIComponent.Label>()[3]
	val rightsideList = state.componentsList.filterIsInstance<RHMIComponent.List>()[0]
	val image = state.componentsList.filterIsInstance<RHMIComponent.Image>()[0]
	val imageLoading = state.componentsList.filterIsInstance<RHMIComponent.Label>()[4]
	val statusIcon = state.componentsList.filterIsInstance<RHMIComponent.Image>()[1]
	val statusLabel = state.componentsList.filterIsInstance<RHMIComponent.Label>()[5]

	private val playButton = state.toolbarComponentsList[0]
	private val prevParagraphButton = state.toolbarComponentsList[1]
	private val nextParagraphButton = state.toolbarComponentsList[2]
	private val previousButton = state.toolbarComponentsList[6]
	private val nextButton = state.toolbarComponentsList[7]

	fun initWidgets() {
		if (debugVisible) {
			hidden1Label.setVisible(true)
		}

		globalCoroutineScope.launch {
			model.article.collectLatest { article ->   // new article was opened in ReadView
				val htmlContents = Utils.parseHtml(article?.article?.fullContent ?: "")
				val contents = Utils.formatForReadout(htmlContents)
				controller.loadLines(contents)
			}
		}

		playButton.getAction()?.asRAAction()?.rhmiActionCallback = RHMIActionButtonCallback {
			if (controller.isActive) {
				controller.pause()
			} else {
				controller.play()
			}
		}
		prevParagraphButton.getAction()?.asRAAction()?.rhmiActionCallback = RHMIActionButtonCallback {
			controller.prevLine()
		}
		nextParagraphButton.getAction()?.asRAAction()?.rhmiActionCallback = RHMIActionButtonCallback {
			controller.nextLine()
		}
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

		// update playback state icons
		globalCoroutineScope.launch {
			controller.activeState.collectLatest {
				val stateImageId = when (it) {
					ReadoutState.BUSY -> CarAppSharedAssetResources.IMG_STATUS_ACTIVE
					ReadoutState.ACTIVE -> CarAppSharedAssetResources.IMG_STATUS_ACTIVE
					ReadoutState.PAUSED -> CarAppSharedAssetResources.IMG_STATUS_PAUSED
					else -> CarAppSharedAssetResources.IMG_STATUS_STOPPED
				}
				val stateTextId = when (it) {
					ReadoutState.BUSY -> CarAppSharedAssetResources.TXT_STATUS_ACTIVE
					ReadoutState.ACTIVE -> CarAppSharedAssetResources.TXT_STATUS_ACTIVE
					ReadoutState.PAUSED -> CarAppSharedAssetResources.TXT_STATUS_PAUSED
					else -> CarAppSharedAssetResources.TXT_STATUS_STOPPED
				}

				statusIcon.getModel()?.asImageIdModel()?.imageId = stateImageId
				statusLabel.getModel()?.asTextIdModel()?.textId = stateTextId

				val cmdImageId = when (it) {
					ReadoutState.BUSY -> CarAppSharedAssetResources.IMG_PAUSE
					ReadoutState.ACTIVE -> CarAppSharedAssetResources.IMG_PAUSE
					else -> CarAppSharedAssetResources.IMG_PLAY
				}
				val cmdTextId = when (it) {
					ReadoutState.BUSY -> CarAppSharedAssetResources.TXT_PAUSE
					ReadoutState.ACTIVE -> CarAppSharedAssetResources.TXT_PAUSE
					else -> CarAppSharedAssetResources.TXT_PLAY
				}

				playButton.getImageModel()?.asImageIdModel()?.imageId = cmdImageId
				playButton.getTooltipModel()?.asTextIdModel()?.textId = cmdTextId
			}
		}

		// update back/next icons
		globalCoroutineScope.launch {
			model.canSkipPrevious.collectLatest {
				previousButton.setProperty(RHMIProperty.PropertyId.ENABLED, it)
			}
		}
		globalCoroutineScope.launch {
			model.canSkipNext.collectLatest {
				nextButton.setProperty(RHMIProperty.PropertyId.ENABLED, it)
			}
		}
		// update labels
		globalCoroutineScope.launch {
			if (debugVisible) {
				controller.debugText.collectLatest {
					hidden1Label.getModel()?.asRaDataModel()?.value = it
				}
			}
		}
		globalCoroutineScope.launch {
			controller.currentLine.collectLatest {
				curLineLabel.getModel()?.asRaDataModel()?.value = it
			}
		}

		state.visibleCallback = VisibleCallback { visible ->
			if (visible) {
				controller.play()
			}
		}
	}
}