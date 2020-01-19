package com.guet.flexbox

import android.view.View
import com.guet.flexbox.transaction.HttpTransaction
import com.guet.flexbox.transaction.RefreshTransaction
import java.lang.ref.WeakReference

internal class ForwardContext : HostingContext() {

    private var targetImpl: WeakReference<HostingContext>? = null

    var target: HostingContext?
        set(value) {
            targetImpl = if (value != null) {
                WeakReference(value)
            } else {
                null
            }
        }
        get() {
            return targetImpl?.get()
        }

    override fun send(source: View, values: Array<out Any?>) {
        target?.send(source, values)
    }

    override fun refresh(source: View): RefreshTransaction? {
        return target?.refresh(source)
    }

    override fun http(source: View): HttpTransaction? {
        return target?.http(source)
    }
}