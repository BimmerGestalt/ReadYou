package io.bimmergestalt.reader.carapp.views

import io.bimmergestalt.idriveconnectkit.rhmi.RHMIComponent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState

class ReadView(val state: RHMIState.ToolbarState) {
	private val bodyList = state.componentsList.filterIsInstance<RHMIComponent.List>()[0]
	private val image = state.componentsList.filterIsInstance<RHMIComponent.Image>()[0]
	private val readoutButton = state.toolbarComponentsList[3]
	private val previousButton = state.toolbarComponentsList[5]
	private val nextButton = state.toolbarComponentsList[6]

	fun initWidgets() {
		state.getTextModel()?.asRaDataModel()?.value = "Feed Title"
	}

	fun getReadoutDest() = readoutButton.getAction()?.asHMIAction()?.target!!
}