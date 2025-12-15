@file:OptIn(ExperimentalSettingsApi::class)

package com.mrboomdev.v2rayng2.settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.getBooleanStateFlow
import com.russhwolf.settings.coroutines.getIntStateFlow
import com.russhwolf.settings.coroutines.getStringStateFlow
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import java.util.concurrent.CopyOnWriteArrayList

sealed interface Setting<T> {
    val value: T
    fun set(value: T)
    fun observe(coroutineScope: CoroutineScope): StateFlow<T>
}

@Composable
fun <T> Setting<T>.observeAsState() = observe(rememberCoroutineScope()).collectAsState()

class BooleanSetting(
    val key: String,
    val defaultValue: Boolean
): Setting<Boolean> {
    override val value get() = impl.getBoolean(key, defaultValue)
    override fun set(value: Boolean) { impl[key] = value }
    override fun observe(coroutineScope: CoroutineScope) = impl.getBooleanStateFlow(coroutineScope, key, defaultValue)
}

class IntSetting(
    val key: String,
    val defaultValue: Int
): Setting<Int> {
    override val value get() = impl.getInt(key, defaultValue)
    override fun set(value: Int) { impl[key] = value }
    override fun observe(coroutineScope: CoroutineScope) = impl.getIntStateFlow(coroutineScope, key, defaultValue)
}

class StringListSetting(
    val key: String,
    val defaultValue: List<String>
) : Setting<List<String>>, MutableList<String> {
    private val _defaultValue by lazy { Json.encodeToString(defaultValue) }
    private val list by lazy { CopyOnWriteArrayList(currentValue) }

    private val currentValue: List<String>
        get() = Json.decodeFromString(impl.getString(key, _defaultValue))

    private fun save() {
        impl[key] = Json.encodeToString(list.toList())
    }

    override val value: List<String>
        get() = list

    override fun set(value: List<String>) {
        synchronized(this) {
            list.clear()
            list.addAll(value)
            save()
        }
    }

    override fun observe(
        coroutineScope: CoroutineScope
    ) = impl.getStringStateFlow(coroutineScope, key, _defaultValue).map {
        Json.decodeFromString<List<String>>(it)
    }.stateIn(coroutineScope, SharingStarted.Eagerly, currentValue)

    override val size: Int get() = list.size
    override fun contains(element: String) = list.contains(element)
    override fun containsAll(elements: Collection<String>) = list.containsAll(elements)
    override fun get(index: Int): String = list[index]
    override fun indexOf(element: String) = list.indexOf(element)
    override fun isEmpty() = list.isEmpty()
    override fun iterator() = list.iterator()
    override fun lastIndexOf(element: String) = list.lastIndexOf(element)
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int) = list.subList(fromIndex, toIndex)

    override fun add(element: String): Boolean {
        return synchronized(this) {
            val result = list.add(element)
            save()
            result
        }
    }

    override fun add(index: Int, element: String) {
        synchronized(this) {
            list.add(index, element)
            save()
        }
    }

    override fun addAll(elements: Collection<String>): Boolean {
        return synchronized(this) {
            val result = list.addAll(elements)
            save()
            result
        }
    }

    override fun addAll(index: Int, elements: Collection<String>): Boolean {
        return synchronized(this) {
            val result = list.addAll(index, elements)
            save()
            result
        }
    }

    override fun clear() {
        synchronized(this) {
            list.clear()
            save()
        }
    }

    override fun remove(element: String): Boolean {
        return synchronized(this) {
            val result = list.remove(element)
            save()
            result
        }
    }

    override fun removeAll(elements: Collection<String>): Boolean {
        return synchronized(this) {
            val result = list.removeAll(elements.toSet())
            save()
            result
        }
    }

    override fun removeAt(index: Int): String {
        return synchronized(this) {
            val result = list.removeAt(index)
            save()
            result
        }
    }

    override fun retainAll(elements: Collection<String>): Boolean {
        return synchronized(this) {
            val result = list.retainAll(elements.toSet())
            save()
            result
        }
    }

    override fun set(index: Int, element: String): String {
        return synchronized(this) {
            val result = list.set(index, element)
            save()
            result
        }
    }
}

class StringSetting(
    val key: String,
    val defaultValue: String
): Setting<String> {
    override val value get() = impl.getString(key, defaultValue)
    override fun set(value: String) { impl[key] = value }
    override fun observe(coroutineScope: CoroutineScope) = impl.getStringStateFlow(coroutineScope, key, defaultValue)
}