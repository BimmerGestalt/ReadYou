package io.bimmergestalt.reader.carapp.views

import io.bimmergestalt.idriveconnectkit.rhmi.RHMIComponent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIModel
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState

class FeedView(val state: RHMIState) {
	val feedList = state.componentsList.filterIsInstance<RHMIComponent.List>().first()

	fun initWidgets() {
		feedList.getModel()?.value = RHMIModel.RaListModel.RHMIListConcrete(3).apply {
			addRow(arrayOf("Category 1"))
			addRow(arrayOf("------"))
			addRow(arrayOf("Feed 1"))
		}
	}
}