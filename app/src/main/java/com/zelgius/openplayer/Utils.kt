package com.zelgius.openplayer

import com.beust.klaxon.Converter
import com.beust.klaxon.Klaxon

inline fun <reified T> String.parseAsJsonArray() = Klaxon().parseArray<T>(this)
inline fun <reified T> String.parseAsJsonObject(converter: Converter? = null) = Klaxon().apply {
    if (converter != null) converter(converter)
}.parse<T>(this)