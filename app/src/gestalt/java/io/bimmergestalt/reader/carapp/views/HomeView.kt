package io.bimmergestalt.reader.carapp.views

import io.bimmergestalt.idriveconnectkit.rhmi.RHMIComponent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIModel
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIProperty
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState

class HomeView(val state: RHMIState) {
	private val feedButton = state.componentsList.filterIsInstance<RHMIComponent.Button>().first()
	private val updatedLabel = state.componentsList.filterIsInstance<RHMIComponent.Label>()[0]
	private val loadingLabel = state.componentsList.filterIsInstance<RHMIComponent.Label>()[1]
	private val offlineLabel = state.componentsList.filterIsInstance<RHMIComponent.Label>()[2]
	private val entriesList = state.componentsList.filterIsInstance<RHMIComponent.List>().first()
	private val updateButton = state.optionComponentsList.filterIsInstance<RHMIComponent.Button>().first()

	fun initWidgets() {
		feedButton.getModel()?.asRaDataModel()?.value = "Unread"
		feedButton.setProperty(RHMIProperty.PropertyId.VISIBLE, true)

		entriesList.getModel()?.value = RHMIModel.RaListModel.RHMIListConcrete(2).apply {
			addRow(arrayOf("•", "Title text\nFeed Name"))
		}
	}

	fun getFeedButtonDest() = feedButton.getAction()?.asHMIAction()?.target!!
	fun getEntryListDest() = entriesList.getAction()?.asHMIAction()?.target!!
}