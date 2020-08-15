package dev.luboganev.progress

import android.R
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import dev.luboganev.progress.databinding.ActivityMainBinding
import dev.luboganev.progress.view.CircularProgressView


class MainActivity : AppCompatActivity() {

    private val viewBinding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.run {
            updateRingStrokeWidth(progressView, strokeWidthSeekBar.progress)
            strokeWidthSeekBar.setOnSeekBarChangeListener(SeekBarChangeListener {
                updateRingStrokeWidth(progressView, it)
            })

            updateRingStrokeColor(
                progressView,
                colorRedSeekBar.progress,
                colorGreenSeekBar.progress,
                colorBlueSeekBar.progress
            )
            colorRedSeekBar.setOnSeekBarChangeListener(SeekBarChangeListener {
                updateRingStrokeColor(
                    progressView,
                    colorRedSeekBar.progress,
                    colorGreenSeekBar.progress,
                    colorBlueSeekBar.progress
                )
            })
            colorGreenSeekBar.setOnSeekBarChangeListener(SeekBarChangeListener {
                updateRingStrokeColor(
                    progressView,
                    colorRedSeekBar.progress,
                    colorGreenSeekBar.progress,
                    colorBlueSeekBar.progress
                )
            })
            colorBlueSeekBar.setOnSeekBarChangeListener(SeekBarChangeListener {
                updateRingStrokeColor(
                    progressView,
                    colorRedSeekBar.progress,
                    colorGreenSeekBar.progress,
                    colorBlueSeekBar.progress
                )
            })
        }
    }

    private fun updateRingStrokeWidth(progressView: CircularProgressView, strokeWidthDps: Int) {
        progressView.setStrokeWidth(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                strokeWidthDps.toFloat(),
                resources.displayMetrics
            )
        )
    }

    private fun updateRingStrokeColor(progressView: CircularProgressView, r: Int, g: Int, b: Int) {
        progressView.setTint(Color.argb(255, r, g, b))
    }

    private fun getThemeAccentColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, value, true)
        return value.data
    }
}