package org.opencrow.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * A small (?) icon that shows a floating tooltip on tap.
 * The tooltip overlays the UI without pushing content around.
 * Dismisses when tapping anywhere else or when any field gains focus.
 *
 * Usage:
 * ```
 * Row(verticalAlignment = Alignment.CenterVertically) {
 *     Text("Notification Chat ID")
 *     InfoTooltip("You can find your chat ID on your Telegram profile page.")
 * }
 * ```
 */
@Composable
fun InfoTooltip(text: String, modifier: Modifier = Modifier) {
    var show by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .padding(start = 4.dp)
            .onFocusChanged { if (it.hasFocus) show = false }
    ) {
        Text(
            text = "?",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                .clickable {
                    if (show) {
                        show = false
                    } else {
                        focusManager.clearFocus()
                        show = true
                    }
                }
                .wrapContentSize(Alignment.Center)
        )

        if (show) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, -4),
                onDismissRequest = { show = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shape = MaterialTheme.shapes.small,
                    shadowElevation = 6.dp,
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .widthIn(max = 240.dp)
                    )
                }
            }
        }
    }
}
