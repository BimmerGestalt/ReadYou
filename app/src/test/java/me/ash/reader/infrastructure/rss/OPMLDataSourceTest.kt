package me.ash.reader.infrastructure.rss

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.model.group.GroupWithFeed
import me.ash.reader.infrastructure.preference.PreferencesReader
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal const val OPML_TEMPLATE: String = """
<?xml version="1.0" encoding="UTF-8"?>
<opml version="1.0">
    <head>
        <title>Import OPML Unit Test👿</title>
    </head>
     <body>
        <outline text="Blogs" title="Blogs">
            {{var}}
        </outline>
    </body>
</opml>
"""

@RunWith(MockitoJUnitRunner::class)
class OPMLDataSourceTest {

    @Mock
    private lateinit var mockPreferences: PreferencesReader

    @Mock
    private lateinit var mockIODispatcher: CoroutineDispatcher

    private lateinit var opmlDataSource: OPMLDataSource

    private val defaultGroup = Group(id = "1", name = "Default", accountId = 1)

    private lateinit var mockObject: OPMLDataSourceTest

    @Before
    fun setUp() {
        mockPreferences = mock<PreferencesReader> {
            on { currentAccountId } doReturn 1
        }
        mockIODispatcher = mock<CoroutineDispatcher> {}
        opmlDataSource = OPMLDataSource(mockPreferences, mockIODispatcher)
    }

    @Test
    fun testEmptyTitle() {
        val opml = fill("""
            <outline type="rss" xmlUrl="https://ash7.io/index.xml" htmlUrl="https://ash7.io"/>
        """)

        runBlocking {
            val result: List<GroupWithFeed> = opmlDataSource.parseFileInputStream(
                inputStream = opml.byteInputStream(Charsets.UTF_8),
                defaultGroup = defaultGroup
            )
            Assert.assertEquals(2, result.size) // includes the defaultGroup and the outline group
        }
    }
}

private fun fill(str: String): String = OPML_TEMPLATE.replace("{{var}}", str).trim()
