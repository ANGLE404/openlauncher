package com.openlauncher.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val LauncherCardShape = RoundedCornerShape(14.dp)
val LauncherLargeShape = RoundedCornerShape(18.dp)
val LauncherControlShape = RoundedCornerShape(10.dp)
val LauncherChipShape = RoundedCornerShape(8.dp)
val LauncherDialogShape = RoundedCornerShape(20.dp)

val LauncherShapes = Shapes(
    extraSmall = LauncherChipShape,
    small = LauncherControlShape,
    medium = LauncherCardShape,
    large = LauncherDialogShape,
)
