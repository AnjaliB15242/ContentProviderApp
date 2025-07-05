package com.example.contentproviderapp

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class RandomString(
    val id: Long = System.currentTimeMillis(),
    val value: String,
    val length: Int,
    val created: String
)

class StringViewModel(
    private val contentResolver: ContentResolver
) : ViewModel() {
    private val _strings = MutableStateFlow<List<RandomString>>(emptyList())
    val strings = _strings.asStateFlow()

    private val _length = MutableStateFlow("10")
    val length = _length.asStateFlow()

    private val uri = Uri.parse("content://com.iav.contestdataprovider/text")

    fun setLength(len: String) {
        _length.value = len
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateString() {
        try {
            val bundle = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, 1)
            }

            contentResolver.query(uri, null, bundle, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val jsonStr = cursor.getString(cursor.getColumnIndexOrThrow("data"))
                    val json = JSONObject(jsonStr).getJSONObject("randomText")

                    val random = RandomString(
                        value = json.getString("value"),
                        length = json.getInt("length"),
                        created = json.getString("created")
                    )
                    _strings.value = _strings.value + random
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteAll() {
        _strings.value = emptyList()
    }

    fun deleteItem(item: RandomString) {
        _strings.value = _strings.value.filterNot { it.id == item.id }
    }
}

class StringViewModelFactory(
    private val resolver: ContentResolver
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StringViewModel(resolver) as T
    }
}