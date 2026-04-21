package org.opencrow.app.ui.screens.chat.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import org.opencrow.app.data.remote.dto.ToolCallDto
import org.opencrow.app.ui.theme.LocalSpacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolCallBubble(toolCalls: List<ToolCallDto>) {
    val spacing = LocalSpacing.current
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(180, easing = FastOutSlowInEasing))
    ) {
        // Collapsed header — always visible, shows tool name + main arg
        Surface(
            onClick = { expanded = !expanded },
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { expanded = !expanded },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val text = toolCalls.joinToString("\n") { "[${it.status}] ${it.name}" }
                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cb.setPrimaryClip(ClipData.newPlainText("tool_calls", text))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = spacing.sm, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Filled.Build,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    toolCalls.joinToString(" · ") { toolCallSummary(it) },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    toolCalls.forEach { call ->
                        val ok = call.status == "success" || call.status == "ok"
                        val err = call.status == "error" || call.status == "failed"
                        Icon(
                            if (ok) Icons.Filled.CheckCircle
                            else if (err) Icons.Filled.Error
                            else Icons.Filled.Build,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = when {
                                ok -> MaterialTheme.colorScheme.tertiary
                                err -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Expanded: output only (args shown in collapsed row)
        if (expanded) {
            Spacer(Modifier.height(3.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                toolCalls.forEach { call ->
                    ToolCallOutput(call)
                }
            }
        }
    }
}

@Composable
private fun ToolCallOutput(call: ToolCallDto) {
    val spacing = LocalSpacing.current
    val ok = call.status == "success" || call.status == "ok"
    val err = call.status == "error" || call.status == "failed"
    val statusColor = when {
        ok -> MaterialTheme.colorScheme.tertiary
        err -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val outputJson = remember(call.output) {
        if (call.output.isNullOrBlank()) null else prettyJsonOrRaw(call.output)
    }

    if (outputJson == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            Icon(
                if (ok) Icons.Filled.CheckCircle else if (err) Icons.Filled.Error else Icons.Filled.Build,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = statusColor
            )
            Text(
                call.name,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                outputJson,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 40,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun toolCallSummary(call: ToolCallDto): String {
    val args = call.arguments
    if (args.isNullOrEmpty()) return call.name
    val priorityKeys = listOf("command", "path", "query", "message", "content", "text", "input", "code", "url", "name", "tool")
    val value = priorityKeys.firstNotNullOfOrNull { args[it]?.toString() }
        ?: args.values.firstOrNull()?.toString()
        ?: return call.name
    val flat = value.replace('\n', ' ').trim()
    val truncated = flat.take(35)
    return if (flat.length > 35) "${call.name}($truncated…)" else "${call.name}($truncated)"
}

private fun prettyJsonOrRaw(text: String): String = try {
    JSONObject(text).toString(2)
} catch (_: Exception) {
    try { JSONArray(text).toString(2) } catch (_: Exception) { text }
}
