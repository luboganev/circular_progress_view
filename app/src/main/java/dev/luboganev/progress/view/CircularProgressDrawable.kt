package dev.luboganev.progress.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.Paint.Cap
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.core.graphics.ColorUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.min

/**
 * A custom circular progress animated drawable
 */
class CircularProgressDrawable : Drawable(), Animatable {
    private var animator: Animator? = null
    private val ring: Ring
    private var rotation = 0f
    private var rotationCount = 0f

    /**
     * Stroke width in pixels of the animation ring
     */
    var strokeWidthPx: Float
        get() = ring.strokeWidth
        set(value) {
            val newRadius = ring.centerRadius - (value - ring.strokeWidth)
            if (newRadius > 0) {
                ring.centerRadius = newRadius
                ring.strokeWidth = value
            } else {
                ring.strokeWidth =
                    DEFAULT_STROKE_WIDTH
            }
            updateSize()
        }

    /**
     * Color for tinting the animation ring
     */
    var tintColor: Int
        get() = ring.tintColor
        set(value) {
            ring.tintColor = value
            invalidateSelf()
        }

    init {
        ring = Ring()
        animator = ValueAnimator.ofFloat(0f, 1f).also { setupAnimator(it) }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateSize()
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        canvas.save()
        canvas.rotate(rotation, bounds.exactCenterX(), bounds.exactCenterY())
        ring.draw(canvas, bounds)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {}

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun isRunning(): Boolean {
        return animator?.isRunning == true
    }

    override fun start() {
        animator?.cancel()
        ring.storeOriginals()
        ring.resetOriginals()
        animator?.duration = ANIMATION_DURATION.toLong()
        animator?.start()
    }

    override fun stop() {
        animator?.cancel()
        rotation = 0f
        ring.resetOriginals()
        invalidateSelf()
    }

    private fun updateSize() {
        val minSize = min(bounds.width(), bounds.height())
        ring.centerRadius = (minSize / 2.0f) - (ring.strokeWidth)
    }

    private fun setupAnimator(animator: ValueAnimator) {
        animator.addUpdateListener { animation ->
            val interpolatedTime = animation.animatedValue as Float
            applyTransformation(interpolatedTime, ring, false)
            invalidateSelf()
        }
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator =
            LINEAR_INTERPOLATOR
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                rotationCount = 0f
            }

            override fun onAnimationEnd(animator: Animator) {
                // do nothing
            }

            override fun onAnimationCancel(animation: Animator) {
                // do nothing
            }

            override fun onAnimationRepeat(animator: Animator) {
                applyTransformation(1f, ring, true)
                ring.storeOriginals()
                rotationCount += 1
            }
        })
        this.animator = animator
    }

    /**
     * Update the ring start and end trim according to current time of the animation.
     */
    private fun applyTransformation(
        interpolatedTime: Float,
        ring: Ring,
        lastFrame: Boolean
    ) {
        if (interpolatedTime != 1f || lastFrame) {
            val startingRotation = ring.startingRotation
            val startTrim: Float
            val endTrim: Float
            if (interpolatedTime < SHRINK_OFFSET) { // Expansion occurs on first half of animation
                ring.isGrowing = true
                val scaledTime =
                    interpolatedTime / SHRINK_OFFSET
                startTrim = ring.startingStartTrim
                endTrim =
                    startTrim + ((DEFAULT_MAX_PROGRESS_ARC - DEFAULT_MIN_PROGRESS_ARC)
                            * MATERIAL_INTERPOLATOR.getInterpolation(
                        scaledTime
                    ) + DEFAULT_MIN_PROGRESS_ARC)
            } else { // Shrinking occurs on second half of animation
                ring.isGrowing = false
                val scaledTime =
                    (interpolatedTime - SHRINK_OFFSET) / (1f - SHRINK_OFFSET)
                endTrim =
                    ring.startingStartTrim + (DEFAULT_MAX_PROGRESS_ARC - DEFAULT_MIN_PROGRESS_ARC)
                startTrim =
                    endTrim - ((DEFAULT_MAX_PROGRESS_ARC - DEFAULT_MIN_PROGRESS_ARC)
                            * (1f - MATERIAL_INTERPOLATOR.getInterpolation(
                        scaledTime
                    ))
                            + DEFAULT_MIN_PROGRESS_ARC)
            }
            val rotation =
                startingRotation + DEFAULT_RING_ROTATION * interpolatedTime
            val groupRotation =
                GROUP_FULL_ROTATION * (interpolatedTime + rotationCount)
            ring.startTrim = startTrim
            ring.endTrim = endTrim
            ring.rotation = rotation
            this.rotation = groupRotation
        }
    }


    /**
     * A private class to do all the drawing of CircularProgressDrawable spinner.
     * This class is to separate drawing from animation.
     */
    private class Ring {
        var tintColor = Color.BLACK
            set(value) {
                tintColorTransparent = ColorUtils.setAlphaComponent(value, 0)
                field = value
            }
        private var tintColorTransparent = Color.TRANSPARENT
        val tempBounds = RectF()
        val tempGradientColors = IntArray(5)
        val tempGradientPositions = FloatArray(5)
        val paint = Paint()
        var startTrim = 0f
        var endTrim = 0f
        var rotation = 0f

        var startingStartTrim = 0f
        var startingEndTrim = 0f

        /**
         * The amount the progress spinner is currently rotated, between [0..1].
         */
        var startingRotation = 0f

        /**
         * Inner radius in px of the circle the progress spinner arc traces
         */
        var centerRadius =
            DEFAULT_RADIUS

        /**
         * The stroke width of the progress spinner in pixels.
         */
        var strokeWidth: Float =
            DEFAULT_STROKE_WIDTH
            set(value) {
                field = value
                paint.strokeWidth = strokeWidth
            }

        /**
         * During the animation the ring is growing or shrinking. This should be set accordingly
         */
        var isGrowing: Boolean = true

        init {
            paint.strokeCap = Cap.SQUARE
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE
        }

        /**
         * Draw the progress spinner
         */
        fun draw(c: Canvas, bounds: Rect) {
            val arcBounds = tempBounds
            val arcRadius = centerRadius + strokeWidth / 2f
            arcBounds.set(
                bounds.centerX() - arcRadius,
                bounds.centerY() - arcRadius,
                bounds.centerX() + arcRadius,
                bounds.centerY() + arcRadius
            )
            val startAngle = (startTrim + rotation) * 360
            val endAngle = (endTrim + rotation) * 360
            val sweepAngle = endAngle - startAngle

            // Calculate the sweep gradient angles
            var sweepGradientStart: Float
            var sweepGradientEnd: Float
            if (isGrowing) {
                sweepGradientStart = (startTrim + rotation) % 1.0f
                sweepGradientEnd = sweepGradientStart + DEFAULT_MAX_PROGRESS_ARC
            } else {
                sweepGradientEnd = (endTrim + rotation) % 1.0f
                sweepGradientStart = sweepGradientEnd - DEFAULT_MAX_PROGRESS_ARC
                if (sweepGradientStart < 0) {
                    sweepGradientEnd += 1.0f
                    sweepGradientStart += 1.0f
                }
            }
            var sweepGradientPoint = 0
            if (sweepGradientEnd < 1.0f) {
                tempGradientPositions[sweepGradientPoint] = 0.0f
                tempGradientColors[sweepGradientPoint] = tintColorTransparent
                sweepGradientPoint++
                tempGradientPositions[sweepGradientPoint] = sweepGradientStart
                tempGradientColors[sweepGradientPoint] = tintColorTransparent
                sweepGradientPoint++
                tempGradientPositions[sweepGradientPoint] =
                    sweepGradientStart + DEFAULT_MAX_PROGRESS_ARC
                tempGradientColors[sweepGradientPoint] = tintColor
                sweepGradientPoint++
                tempGradientPositions[sweepGradientPoint] =
                    sweepGradientStart + DEFAULT_MAX_PROGRESS_ARC + 0.0001f
                tempGradientColors[sweepGradientPoint] = tintColorTransparent
                sweepGradientPoint++
                tempGradientPositions[sweepGradientPoint] = 1.0f
                tempGradientColors[sweepGradientPoint] = tintColorTransparent
            } else {
                val actualSweepGradientStart = sweepGradientEnd - 1.0f
                val alphaFactor = 1.0f / DEFAULT_MAX_PROGRESS_ARC
                val remainingSweepUntil1 = DEFAULT_MAX_PROGRESS_ARC - actualSweepGradientStart
                val alphaAt1And0 =
                    ((remainingSweepUntil1 * alphaFactor) * 255.0f).toInt().coerceIn(0..255)
                val colorAt1And0 = ColorUtils.setAlphaComponent(tintColor, alphaAt1And0)

                tempGradientPositions[sweepGradientPoint] = 0.0f
                tempGradientColors[sweepGradientPoint] = colorAt1And0
                sweepGradientPoint++
                tempGradientPositions[sweepGradientPoint] = actualSweepGradientStart
                tempGradientColors[sweepGradientPoint] = tintColor
                sweepGradientPoint++
                tempGradientPositions[sweepGradientPoint] = actualSweepGradientStart - 0.0001f
                tempGradientColors[sweepGradientPoint] = tintColorTransparent
                sweepGradientPoint++
                tempGradientPositions[sweepGradientPoint] = sweepGradientStart
                tempGradientColors[sweepGradientPoint] = tintColorTransparent
                sweepGradientPoint++
                tempGradientPositions[sweepGradientPoint] = 1.0f
                tempGradientColors[sweepGradientPoint] = colorAt1And0
            }

            paint.shader = SweepGradient(
                bounds.exactCenterX(),
                bounds.exactCenterY(),
                tempGradientColors,
                tempGradientPositions
            )
            c.drawArc(arcBounds, startAngle, sweepAngle, false, paint)
        }

        /**
         * If the start / end trim are offset to begin with, store them so that animation starts
         * from that offset.
         */
        fun storeOriginals() {
            startingStartTrim = startTrim
            startingEndTrim = endTrim
            startingRotation = rotation
        }

        /**
         * Reset the progress spinner to default rotation, start and end angles.
         */
        fun resetOriginals() {
            startingStartTrim = 0f
            startingEndTrim = 0f
            startingRotation = 0f
            startTrim = 0f
            endTrim = 0f
            rotation = 0f
            isGrowing = true
        }
    }

    companion object {
        private val LINEAR_INTERPOLATOR: Interpolator = LinearInterpolator()
        private val MATERIAL_INTERPOLATOR: Interpolator = FastOutSlowInInterpolator()

        /**
         * The value in the material interpolator for animating the drawable at which
         * the grow should stop and shrink should start
         */
        private const val SHRINK_OFFSET = 0.5f

        /** The duration of a single progress spin in milliseconds.  */
        private const val ANIMATION_DURATION = 1332

        /** Full rotation that's done for the animation duration in degrees.  */
        private const val GROUP_FULL_ROTATION = 1080f / 5f

        /** Maximum length of the progress arc during the animation.  */
        private const val DEFAULT_MAX_PROGRESS_ARC = 0.5f

        /** Minimum length of the progress arc during the animation.  */
        private const val DEFAULT_MIN_PROGRESS_ARC = 0.0f

        /** Rotation applied to ring during the animation, to complete it to a full circle */
        private const val DEFAULT_RING_ROTATION =
            1f - (DEFAULT_MAX_PROGRESS_ARC - DEFAULT_MIN_PROGRESS_ARC)

        private const val DEFAULT_STROKE_WIDTH = 4.0f

        private const val DEFAULT_RADIUS = 24.0f
    }
}