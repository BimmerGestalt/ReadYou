package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.get
import javax.inject.Inject

/**
 * An injectable Preferences wrapper object,
 * so that consumers can receive a mock during tests
 */
class PreferencesReader @Inject constructor(
	@ApplicationContext
	appContext: Context
) {
	private val dataStore: DataStore<Preferences> = appContext.dataStore

	val skipVersionNumber: String
		get() = this.dataStore.get(DataStoreKey.skipVersionNumber) ?: ""
	val isFirstLaunch: Boolean
		get() = this.dataStore.get(DataStoreKey.isFirstLaunch) ?: true
	val currentAccountId: Int
		get() = this.dataStore.get(DataStoreKey.currentAccountId) ?: 1
	val currentAccountType: Int
		get() = this.dataStore.get(DataStoreKey.currentAccountType) ?: 1

	val initialPage: Int
		get() = this.dataStore.get(DataStoreKey.initialPage) ?: 0
	val initialFilter: Int
		get() = this.dataStore.get(DataStoreKey.initialFilter) ?: 2

	val languages: Int
		get() = this.dataStore.get(DataStoreKey.languages) ?: 0
}