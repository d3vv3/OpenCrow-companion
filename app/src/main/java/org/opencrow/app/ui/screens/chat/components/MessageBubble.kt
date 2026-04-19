package org.opencrow.app.ui.screens.chat.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.opencrow.app.data.remote.dto.MessageDto
import org.opencrow.app.ui.components.MarkdownText
import org.opencrow.app.ui.screens.chat.Attachment
import org.opencrow.app.ui.theme.LocalSpacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageDto,
    isTranscribed: Boolean = false,
    attachments: List<Attachment> = emptyList()
) {
    val isUser = message.role == "user"
    val isSystem = message.role == "system"
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, end = spacing.sm)
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }

        Surface(
            color = when {
                isUser -> MaterialTheme.colorScheme.primaryContainer
                isSystem -> MaterialTheme.colorScheme.surfaceContainerHigh
                else -> MaterialTheme.colorScheme.surfaceContainerHigh
            },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("message", message.content))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (isSystem) {
                    Text(
                        "System",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(spacing.xs))
                }

                // Render image attachment previews
                if (attachments.any { it.isImage }) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        for (att in attachments.filter { it.isImage }) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(att.uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = att.name,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(spacing.sm))
                            )
                        }
                    }
                    if (message.content.isNotBlank()) {
                        Spacer(Modifier.height(spacing.sm))
                    }
                }

                // Render non-image file attachment chips
                val fileAttachments = attachments.filter { !it.isImage }
                if (fileAttachments.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                        for (att in fileAttachments) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                )
                                Spacer(Modifier.width(spacing.xs))
                                Text(
                                    text = att.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (message.content.isNotBlank()) {
                        Spacer(Modifier.height(spacing.sm))
                    }
                }

                if (message.content.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        if (isUser && isTranscribed) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = "Transcribed",
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(end = 4.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }
                        MarkdownText(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
