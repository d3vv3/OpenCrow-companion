package org.opencrow.app.ui.screens.chat.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.opencrow.app.data.remote.dto.ConversationDto
import org.opencrow.app.ui.theme.LocalSpacing
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistorySheet(
    visible: Boolean,
    conversations: List<ConversationDto>,
    activeId: String?,
    showSystemChats: Boolean,
    onToggleSystemChats: (Boolean) -> Unit,
    onSelectConversation: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val spacing = LocalSpacing.current

    var shouldRender by remember { mutableStateOf(visible) }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "historySheet",
        finishedListener = { v -> if (v == 0f) shouldRender = false }
    )
    LaunchedEffect(visible) { if (visible) shouldRender = true }

    if (!shouldRender) return

    val drawerWidthPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx() * 0.82f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
    ) {
        // Scrim — only visual, no pointer input
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress * 0.4f }
                .background(MaterialTheme.colorScheme.scrim)
        )

        // Drawer panel — graphicsLayer translationX avoids layout on every frame
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .align(Alignment.CenterStart)
                .graphicsLayer { translationX = (progress - 1f) * drawerWidthPx }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { }
                .pointerInput(Unit) {
                    var drag = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dx -> drag += dx },
                        onDragEnd = {
                            if (drag < -60f) onDismiss()
                            drag = 0f
                        },
                        onDragCancel = { drag = 0f }
                    )
                },
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = spacing.md, vertical = spacing.md)
                ) {
                    Text(
                        "Chats",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )

                // Chat list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = spacing.xs,
                        end = spacing.xs,
                        top = spacing.xs,
                        bottom = spacing.xs
                    )
                ) {
                    itemsIndexed(conversations, key = { _, conv -> conv.id }) { index, conv ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = spacing.xs),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                            )
                        }
                        ConversationRow(
                            conv = conv,
                            isActive = conv.id == activeId,
                            onSelect = {
                                onSelectConversation(conv.id)
                                onDismiss()
                            }
                        )
                    }
                }

                // Bottom: system chats toggle
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "System chats",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = showSystemChats,
                        onCheckedChange = onToggleSystemChats,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conv: ConversationDto,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    val spacing = LocalSpacing.current
    val isTelegram = conv.channel == "telegram" ||
            conv.title.contains("[telegram]", ignoreCase = true)
    val isScheduled = conv.automationKind == "scheduled_task"

    val displayTitle = remember(conv.title) {
        conv.title.replace("[telegram]", "", ignoreCase = true).trim().ifBlank { "Untitled" }
    }
    val relativeTime = remember(conv.updatedAt) { formatRelativeDate(conv.updatedAt) }

    Surface(
        onClick = onSelect,
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
                // Pills row
                val pills = buildList {
                    if (isTelegram) add(Pair("telegram", MaterialTheme.colorScheme.tertiary))
                    else if (conv.isAutomatic && conv.automationKind != null) {
                        val pillColor = if (isScheduled) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.primary
                        add(Pair(conv.automationKind, pillColor))
                    }
                }
                if (pills.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        pills.forEach { (text, color) -> TypePill(text, color) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypePill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(50.dp))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(50.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
private val absFormat = SimpleDateFormat("MMM d", Locale.getDefault())

private fun formatRelativeDate(iso: String): String {
    val date = try { isoParser.parse(iso.take(19)) } catch (_: Exception) { return iso.take(10) }
        ?: return iso.take(10)
    val diffMs = Date().time - date.time
    val diffMin = diffMs / 60_000
    val diffHours = diffMin / 60
    val diffDays = diffHours / 24
    return when {
        diffMin < 1 -> "now"
        diffMin < 60 -> "${diffMin}m"
        diffHours < 24 -> "${diffHours}h"
        diffDays == 1L -> "Yesterday"
        diffDays < 7 -> "${diffDays}d"
        else -> absFormat.format(date)
    }
}
