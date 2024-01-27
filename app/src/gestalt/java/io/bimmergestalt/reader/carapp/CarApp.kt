package io.bimmergestalt.reader.carapp

import android.util.Log
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import de.bmw.idrive.BMWRemoting
import de.bmw.idrive.BMWRemotingServer
import de.bmw.idrive.BaseBMWRemotingClient
import io.bimmergestalt.idriveconnectkit.CDSProperty
import io.bimmergestalt.idriveconnectkit.IDriveConnection
import io.bimmergestalt.idriveconnectkit.Utils.rhmi_setResourceCached
import io.bimmergestalt.idriveconnectkit.android.IDriveConnectionStatus
import io.bimmergestalt.idriveconnectkit.android.security.SecurityAccess
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIApplication
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIApplicationEtch
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIApplicationIdempotent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIApplicationSynchronized
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIComponent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState
import io.bimmergestalt.reader.carapp.views.FeedView
import io.bimmergestalt.reader.carapp.views.HomeView
import io.bimmergestalt.reader.carapp.views.ReadView
import io.bimmergestalt.reader.carapp.views.ReadoutView
import me.ash.reader.domain.service.RssService

const val TAG = "ReaderGestalt"
class CarApp(val iDriveConnectionStatus: IDriveConnectionStatus, securityAccess: SecurityAccess,
             val carAppResources: CarAppSharedAssetResources,
             val rssService: RssService, workManager: WorkManager
) {

	val carConnection: BMWRemotingServer
	val carApp: RHMIApplication
	val readoutController: ReadoutController
	val model: Model = Model(workManager)
	val homeView: HomeView
	val feedView: FeedView
	val readView: ReadView
	val readoutView: ReadoutView

	init {
		Log.i(TAG, "Starting connecting to car")
		val carappListener = CarAppListener()
		carConnection = IDriveConnection.getEtchConnection(iDriveConnectionStatus.host ?: "127.0.0.1", iDriveConnectionStatus.port ?: 8003, carappListener)
		val appCert = carAppResources.getAppCertificate(iDriveConnectionStatus.brand ?: "").readBytes()
		val sas_challenge = carConnection.sas_certificate(appCert)
		val sas_response = securityAccess.signChallenge(challenge = sas_challenge)
		carConnection.sas_login(sas_response)

		carApp = createRhmiApp()
		readoutController = ReadoutController.build(carApp, "News")
		val destStateId = carApp.components.values.filterIsInstance<RHMIComponent.EntryButton>().first().getAction()?.asHMIAction()?.target!!
		homeView = HomeView(carApp.states[destStateId] as RHMIState, rssService, model)
		feedView = FeedView(carApp.states[homeView.getFeedButtonDest()]!!, rssService, model)
		readView = ReadView(carApp.states[homeView.getEntryListDest()] as RHMIState.ToolbarState, model)
		readoutView = ReadoutView(carApp.states[readView.getReadoutDest()] as RHMIState.ToolbarState, readoutController, model)

		initWidgets()

		createCds()

		Log.i(TAG, "CarApp running")
	}

	private fun createRhmiApp(): RHMIApplication {
		// create the app in the car
		val rhmiHandle = carConnection.rhmi_create(null, BMWRemoting.RHMIMetaData("io.bimmergestalt.reader", BMWRemoting.VersionInfo(0, 1, 0), "io.bimmergestalt.reader", "io.bimmergestalt"))
		carConnection.rhmi_setResourceCached(rhmiHandle, BMWRemoting.RHMIResourceType.DESCRIPTION, carAppResources.getUiDescription())
		carConnection.rhmi_setResourceCached(rhmiHandle, BMWRemoting.RHMIResourceType.TEXTDB, carAppResources.getTextsDB("common"))
		carConnection.rhmi_setResourceCached(rhmiHandle, BMWRemoting.RHMIResourceType.IMAGEDB, carAppResources.getImagesDB(iDriveConnectionStatus.brand ?: "common"))
		carConnection.rhmi_setResourceCached(rhmiHandle, BMWRemoting.RHMIResourceType.TEXTDB, carAppResources.getSharedTextsDB("common"))
		carConnection.rhmi_setResourceCached(rhmiHandle, BMWRemoting.RHMIResourceType.IMAGEDB, carAppResources.getSharedImagedDB(iDriveConnectionStatus.brand ?: "common"))
		carConnection.rhmi_initialize(rhmiHandle)

		// register for events from the car
		carConnection.rhmi_addActionEventHandler(rhmiHandle, "io.bimmergestalt.reader", -1)
		carConnection.rhmi_addHmiEventHandler(rhmiHandle, "io.bimmergestalt.reader", -1, -1)

		return RHMIApplicationSynchronized(
			RHMIApplicationIdempotent(
				RHMIApplicationEtch(carConnection, rhmiHandle)
			), carConnection).apply {
			loadFromXML(carAppResources.getUiDescription()!!.readBytes())
		}
	}

	private fun createCds() {
		synchronized(carConnection) {
			val cdsHandle = carConnection.cds_create()
			for (prop in listOf(CDSProperty.HMI_TTS)) {
				carConnection.cds_addPropertyChangedEventHandler(cdsHandle, prop.propertyName, prop.ident.toString(), 200)
				carConnection.cds_getPropertyAsync(cdsHandle, prop.ident.toString(), prop.propertyName)
			}
		}
	}

	private fun initWidgets() {
		carApp.components.values.filterIsInstance<RHMIComponent.EntryButton>().forEach {
			it.getAction()?.asHMIAction()?.getTargetModel()?.asRaIntModel()?.value = homeView.state.id
		}
		homeView.initWidgets()
		feedView.initWidgets()
		readView.initWidgets()
		readoutView.initWidgets()
	}

	fun onDestroy() {
		try {
			Log.i(TAG, "Trying to shut down etch connection")
			IDriveConnection.disconnectEtchConnection(carConnection)
		} catch ( e: java.io.IOError) {
		} catch (e: RuntimeException) {}
	}

	inner class CarAppListener(): BaseBMWRemotingClient() {
		override fun rhmi_onActionEvent(handle: Int?, ident: String?, actionId: Int?, args: MutableMap<*, *>?) {
			try {
				carApp.actions[actionId]?.asRAAction()?.rhmiActionCallback?.onActionEvent(args)
				synchronized(carConnection) {
					carConnection.rhmi_ackActionEvent(handle, actionId, 1, true)
				}
			} catch (e: RHMIActionAbort) {
				// Action handler requested that we don't claim success
				synchronized(carConnection) {
					carConnection.rhmi_ackActionEvent(handle, actionId, 1, false)
				}
			} catch (e: Exception) {
				Log.e(TAG, "Exception while calling onActionEvent handler!", e)
				synchronized(carConnection) {
					carConnection.rhmi_ackActionEvent(handle, actionId, 1, true)
				}
			}
		}

		override fun rhmi_onHmiEvent(handle: Int?, ident: String?, componentId: Int?, eventId: Int?, args: MutableMap<*, *>?) {
			try {
				// generic event handler
				carApp.states[componentId]?.onHmiEvent(eventId, args)
				carApp.components[componentId]?.onHmiEvent(eventId, args)
			} catch (e: Exception) {
				Log.e(TAG, "Received exception while handling rhmi_onHmiEvent", e)
			}
		}

		override fun cds_onPropertyChangedEvent(handle: Int?, ident: String?, propertyName: String?, propertyValue: String?) {
			propertyValue ?: return
			if (propertyName == "hmi.tts") {
				try {
					val hmiTTS = Gson().fromJson(propertyValue, HMITTS::class.java)
					readoutController.onTTSEvent(hmiTTS.TTSState)
				} catch (e: JsonSyntaxException) {
					Log.e(TAG, "Received unexpected hmiTTS $propertyValue", e)
					readoutController.onTTSEvent(TTSState(null, null, null, e.toString(), null))
				}
			}
		}
	}
}