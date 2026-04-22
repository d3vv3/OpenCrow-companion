package org.opencrow.app.ui.screens.chat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.opencrow.app.data.remote.dto.ConversationDto
import org.opencrow.app.ui.theme.LocalSpacing
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    visible: Boolean,
    conversations: List<ConversationDto>,
    activeId: String?,
    showSystemChats: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onToggleSystemChats: (Boolean) -> Unit,
    onSelectConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onLogout: () -> Unit,
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
        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress * 0.4f }
                .background(MaterialTheme.colorScheme.scrim)
        )

        // Drawer panel
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

                val pullState = rememberPullToRefreshState()
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    state = pullState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            state = pullState,
                            isRefreshing = isRefreshing,
                            color = Color(0xFF22D3EE),           // cyan from DESIGN.md
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = spacing.sm, end = spacing.sm,
                        top = spacing.sm, bottom = spacing.sm
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(conversations, key = { _, conv -> conv.id }) { _, conv ->
                        SwipeToDeleteRow(onDelete = { onDeleteConversation(conv.id) }) {
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
                }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
                // Bottom row: [Logout] | [Show system chats ─── Switch]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = spacing.md, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onDismiss(); onLogout() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(spacing.sm))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.width(spacing.sm))
                    Text(
                        "Show system chats",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
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
                Spacer(Modifier.height(spacing.sm))
            }
        }
    }
}

/** Custom swipe-to-delete with haptic, resistance, spring snap-back and animated collapse. */
@Composable
private fun SwipeToDeleteRow(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val heightDp = remember { Animatable(62f) }
    var thresholdPx by remember { mutableFloatStateOf(80f) }
    var rowWidthPx by remember { mutableFloatStateOf(0f) }
    var crossedThreshold by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()
    // errorContainer is too pale in light mode; use a solid red that works in both themes.
    val deleteBackground = if (isDarkTheme) MaterialTheme.colorScheme.errorContainer
                           else Color(0xFFB71C1C)  // Material Red 900 -- clearly visible in light
    val deleteIcon = if (isDarkTheme) MaterialTheme.colorScheme.onErrorContainer
                     else Color.White

    val revealFraction = (offsetX.value.absoluteValue / thresholdPx).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.value.dp)
            .pointerInput(Unit) {
                rowWidthPx = size.width.toFloat()
                thresholdPx = size.width * 0.20f
                detectHorizontalDragGestures(
                    onDragStart = { crossedThreshold = false },
                    onHorizontalDrag = { _, dx ->
                        if (heightDp.value < 62f) return@detectHorizontalDragGestures
                        val current = offsetX.value
                        val absVal = current.absoluteValue
                        val next: Float = if (absVal < thresholdPx) {
                            current + dx
                        } else {
                            val excess = absVal - thresholdPx
                            val resistance = 1f / (1f + excess * 0.018f)
                            current + dx * resistance
                        }
                        scope.launch { offsetX.snapTo(next) }

                        val nowAbove = next.absoluteValue >= thresholdPx
                        if (nowAbove && !crossedThreshold) {
                            crossedThreshold = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else if (!nowAbove && crossedThreshold) {
                            crossedThreshold = false
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    onDragEnd = {
                        if (offsetX.value.absoluteValue >= thresholdPx) {
                            // Fly the card off-screen, then collapse the row height, then delete
                            scope.launch {
                                val flyDir = if (offsetX.value < 0) -rowWidthPx else rowWidthPx
                                launch { offsetX.animateTo(flyDir, tween(160)) }
                                kotlinx.coroutines.delay(80)
                                heightDp.animateTo(0f, tween(180, easing = FastOutSlowInEasing))
                                onDelete()
                            }
                        } else {
                            scope.launch {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                offsetX.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = 1400f))
                            }
                        }
                        crossedThreshold = false
                    },
                    onDragCancel = {
                        scope.launch {
                            offsetX.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = 1400f))
                        }
                        crossedThreshold = false
                    }
                )
            }
    ) {
        // Delete background -- always full-opaque when swiping starts
        if (revealFraction > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(deleteBackground, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = if (offsetX.value > 0) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = deleteIcon)
            }
        }

        // Card slides on top -- offset first so background travels with the card, revealing red beneath
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp))
        ) {
            content()
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
    val isDark = isSystemInDarkTheme()
    val isTelegram = conv.channel == "telegram" ||
            conv.title.contains("[telegram]", ignoreCase = true)

    val displayTitle = remember(conv.title) {
        conv.title
            .replace("[telegram]", "", ignoreCase = true)
            .replace(Regex("^\\[heartbeat\\]\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^heartbeat:\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^scheduled task:\\s*", RegexOption.IGNORE_CASE), "")
            .trim().ifBlank { "Untitled" }
    }
    val relativeTime = remember(conv.updatedAt) { formatRelativeDate(conv.updatedAt) }

    val pillLabel: String? = when {
        isTelegram -> "telegram"
        conv.isAutomatic && conv.automationKind == "heartbeat" -> "heartbeat"
        conv.isAutomatic && conv.automationKind == "scheduled_task" -> "scheduled"
        conv.isAutomatic && conv.automationKind != null -> conv.automationKind
        else -> null
    }

    // Active state colors -- high contrast
    val activeBackground = if (isDark)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    else
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val activeBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.85f else 0.6f)
    val activeTitleColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary
    val activeSubColor = if (isDark) Color.White.copy(alpha = 0.65f)
                         else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)

    Surface(
        onClick = onSelect,
        color = if (isActive) activeBackground else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (isActive) 0.dp else 1.dp,
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isActive) Modifier.border(1.5.dp, activeBorderColor, RoundedCornerShape(12.dp))
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Line 1: title
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isActive) activeTitleColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // Line 2: time (left) + pill (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = relativeTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) activeSubColor
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
                if (pillLabel != null) {
                    val resolvedColor = when (pillLabel) {
                        "telegram"  -> MaterialTheme.colorScheme.tertiary
                        "scheduled" -> MaterialTheme.colorScheme.secondary
                        "heartbeat" -> MaterialTheme.colorScheme.primary
                        else        -> MaterialTheme.colorScheme.primary
                    }
                    TypePill(pillLabel, resolvedColor)
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
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

private fun formatRelativeDate(iso: String): String {
    val date = try { isoParser.parse(iso.take(19)) } catch (_: Exception) { return iso.take(10) }
        ?: return iso.take(10)
    val diffMs = Date().time - date.time
    val diffMin = diffMs / 60_000
    val diffHours = diffMin / 60
    val diffDays = diffHours / 24
    val diffWeeks = diffDays / 7

    return when {
        diffMin < 1 -> "just now"
        diffMin < 60 -> "${diffMin}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays == 1L -> "yesterday"
        diffDays < 7 -> "${diffDays} days ago"
        diffWeeks == 1L -> "last week"
        diffWeeks == 2L -> "2 weeks ago"
        diffWeeks == 3L -> "3 weeks ago"
        diffDays < 45 -> "last month"
        else -> formatAbsoluteDate(date)
    }
}

private fun formatAbsoluteDate(date: Date): String {
    val cal = Calendar.getInstance().apply { time = date }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(date)
    val year = cal.get(Calendar.YEAR)
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    return if (year == currentYear) "$day$suffix $month" else "$day$suffix $month $year"
}
