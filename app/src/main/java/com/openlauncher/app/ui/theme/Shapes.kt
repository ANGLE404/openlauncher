package com.openlauncher.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val LauncherPixelShape = RoundedCornerShape(6.dp)
val LauncherChipShape = RoundedCornerShape(8.dp)
val LauncherControlShape = RoundedCornerShape(10.dp)
val LauncherCardShape = RoundedCornerShape(12.dp)
val LauncherLargeShape = RoundedCornerShape(14.dp)
val LauncherDialogShape = RoundedCornerShape(16.dp)

val LauncherShapes = Shapes(
    extraSmall = LauncherChipShape,
    small = LauncherControlShape,
    medium = LauncherCardShape,
    large = LauncherDialogShape,
)
