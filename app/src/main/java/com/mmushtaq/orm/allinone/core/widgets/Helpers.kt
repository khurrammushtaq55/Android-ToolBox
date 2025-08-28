package com.mmushtaq.orm.allinone.core.widgets

import android.content.Context
import androidx.core.content.ContextCompat
import com.mmushtaq.orm.allinone.features.ruler.RulerUiState
import com.mmushtaq.orm.allinone.features.ruler.RulerUnit

/** Helpers **/
 fun pxToUnit(px: Float, ui: RulerUiState): Float {
    return when (ui.unit) {
        RulerUnit.CM -> px / (ui.pxPerCm * ui.scaleFactor)
        RulerUnit.INCH -> px / (ui.pxPerInch * ui.scaleFactor)
    }
}

 fun Float.format(digits: Int) =
    "%.${digits}f".format(this.coerceIn(-1_000_000f, 1_000_000f))

 fun hasCameraPermission(ctx: Context): Boolean =
    ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
