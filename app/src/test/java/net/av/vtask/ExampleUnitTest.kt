package net.av.vtask

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun itemSerialization() {
        val item = Collection(mutableListOf(), mutableListOf(), "test")
        println(Json.encodeToString<IDataItem>(item))
    }
}