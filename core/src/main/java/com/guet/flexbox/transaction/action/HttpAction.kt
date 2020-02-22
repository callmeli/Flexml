package com.guet.flexbox.transaction.action

class HttpAction(
        val url: String,
        val method: String,
        val prams: Map<String, String>,
        val callback: Callback
) {
    interface Callback {
        fun onResponse(data: String?)

        fun onError()
    }
}