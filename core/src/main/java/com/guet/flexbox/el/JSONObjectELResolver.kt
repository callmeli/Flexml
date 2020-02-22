package com.guet.flexbox.el

import org.json.JSONObject

internal class JSONObjectELResolver(private val isReadOnly: Boolean = false) : ELResolver() {

    override fun getValue(context: ELContext, base: Any?, property: Any?): Any? {
        if (base is JSONObject) {
            context.setPropertyResolved(base, property)
            return base[property.toString()]
        }
        return null
    }

    override fun getType(context: ELContext, base: Any?, property: Any?): Class<*>? {
        if (base is JSONObject) {
            context.setPropertyResolved(base, property)
            return base[property.toString()]?.javaClass
        }
        return null
    }

    override fun setValue(context: ELContext, base: Any?, property: Any?, value: Any?) {
        if (isReadOnly) {
            throw PropertyNotWritableException()
        }
        if (base is JSONObject) {
            context.setPropertyResolved(base, property)
            base.put(property.toString(), value)
        }
    }

    override fun isReadOnly(context: ELContext, base: Any?, property: Any?): Boolean {
        return isReadOnly
    }

    override fun getCommonPropertyType(context: ELContext?, base: Any?): Class<*>? {
        if (base is JSONObject) {
            return String::class.java
        }
        return null
    }

    override fun convertToType(context: ELContext, obj: Any?, type: Class<*>): Any? {
        if (obj is String && type == JSONObject::class.java) {
            try {
                val json = JSONObject(obj)
                context.isPropertyResolved = true
                return json
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return null
    }
}