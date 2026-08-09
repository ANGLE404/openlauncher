package com.openlauncher.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val LauncherCardShape = RoundedCornerShape(16.dp)
val LauncherLargeShape = RoundedCornerShape(20.dp)
val LauncherControlShape = RoundedCornerShape(12.dp)
val LauncherChipShape = RoundedCornerShape(10.dp)
val LauncherDialogShape = RoundedCornerShape(22.dp)

val LauncherShapes = Shapes(
    extraSmall = LauncherChipShape,
    small = LauncherControlShape,
    medium = LauncherCardShape,
    large = LauncherDialogShape,
)
