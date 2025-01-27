package ru.shelq.nework.util

import android.os.Bundle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object idArg : ReadWriteProperty<Bundle,Long?> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): Long? =
        thisRef.getLong(property.name)


    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Long?) {
        if (value != null) {
            thisRef.putLong(property.name,value)
        }
    }
}

object LongArrayArg: ReadWriteProperty<Bundle, LongArray?> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: LongArray?) {
        thisRef.putLongArray(property.name, value ?: LongArray(0))
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): LongArray? =
        thisRef.getLongArray(property.name)
}