package com.zelgius.openplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.beust.klaxon.Converter
import com.beust.klaxon.Klaxon

inline fun <reified T> String.parseAsJsonArray() = Klaxon().parseArray<T>(this)
inline fun <reified T> String.parseAsJsonObject(converter: Converter? = null) = Klaxon().apply {
    if (converter != null) converter(converter)
}.parse<T>(this)

inline fun<T> mutableLiveDataOf(data: T? = null): MutableLiveData<T> = if(data != null) MutableLiveData(data) else MutableLiveData()
inline fun<T> liveDataOf(data: T? = null): LiveData<T> = if(data != null) MutableLiveData(data) else MutableLiveData()