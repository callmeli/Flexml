package com.guet.flexbox.litho.drawable

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.widget.ImageView.ScaleType
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.guet.flexbox.litho.transforms.RenderGroup

class GlideDrawable(
        private val context: Context
) : DrawableWrapper<Drawable>(NoOpDrawable()),
        Target<Drawable> by DelegateTarget() {
    private var width: Int = 0
    private var height: Int = 0

    fun unmount() {
        wrappedDrawable = NoOpDrawable()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        bind(bounds.width(), bounds.height())
    }

    fun bind(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun mount(
            model: Any,
            width: Int,
            height: Int,
            blurRadius: Float,
            blurSampling: Float,
            scaleType: ScaleType,
            leftTop: Float,
            rightTop: Float,
            rightBottom: Float,
            lightBottom: Float
    ) {
        bind(width, height)
        Glide.with(context)
                .load(model)
                .transform(
                        RenderGroup.Builder {
                            this.blurRadius = blurRadius
                            this.blurSampling = blurSampling
                            this.scaleType = scaleType
                            this.leftTop = leftTop
                            this.rightTop = rightTop
                            this.rightBottom = rightBottom
                            this.leftBottom = lightBottom
                        }.build()
                ).into(this)
    }

    override fun getSize(cb: SizeReadyCallback) {
        cb.onSizeReady(width, height)
    }

    override fun onResourceReady(
            resource: Drawable,
            transition: Transition<in Drawable>?
    ) {
        resource.bounds = bounds
        wrappedDrawable = resource
        invalidateSelf()
    }
}