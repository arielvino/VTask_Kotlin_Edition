package net.av.vtask

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("item_type")
@Serializable
sealed interface IDataItem {
    companion object{

    }

    val title: String
    val referrers: MutableList<String>
}