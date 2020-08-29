package dev.luboganev.progress.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import dev.luboganev.progress.R


class CircularProgressView : View {

    private val drawable: CircularProgressDrawable = CircularProgressDrawable()
    private val classInitializedCheck: Unit? = Unit

    constructor(context: Context) : super(context) {
        initCustomView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initCustomView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initCustomView(context, attrs)
    }

    private fun initCustomView(context: Context, attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressView)
        if (typeArray.hasValue(R.styleable.CircularProgressView_cpv_strokeWidth)) {
            drawable.strokeWidthPx =
                typeArray.getDimension(R.styleable.CircularProgressView_cpv_strokeWidth, 1.0f)
        }
        if (typeArray.hasValue(R.styleable.CircularProgressView_cpv_tint)) {
            drawable.tintColor =
                typeArray.getColor(R.styleable.CircularProgressView_cpv_tint, Color.BLACK)
        }
        typeArray.recycle()

        background = drawable

        addOnAttachStateChangeListener(object: OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(p0: View?) {
                drawable.stop()
            }

            override fun onViewAttachedToWindow(p0: View?) {
                if (visibility == VISIBLE) {
                    drawable.start()
                }
            }
        })
    }

    fun setTint(@ColorInt color: Int) {
        drawable.tintColor = color
    }

    fun setTintResource(@ColorRes colorRes: Int) {
        drawable.tintColor = ContextCompat.getColor(context, colorRes)
    }

    fun setStrokeWidth(strokeWidthPx: Float) {
        drawable.strokeWidthPx = strokeWidthPx
    }

    fun setStrokeWidthRes(@DimenRes strokeWidthRes: Int) {
        drawable.strokeWidthPx = resources.getDimension(strokeWidthRes)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        // This method gets called on some Android versions and some phone vendors
        // in the super constructor. Due to how Kotlin works, it means that drawable
        // will be null, although it is initialized in this class. To prevent runtime
        // null pointer exceptions caused by this, we use a simple val and we check if
        // it is initialized, before doing anything else.
        if (classInitializedCheck == null) return

        if (visibility == VISIBLE && isAttachedToWindow) {
            drawable.start()
        } else {
            drawable.stop()
        }
    }
}