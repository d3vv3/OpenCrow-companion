package org.opencrow.app.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.opencrow.app.ui.screens.chat.Attachment
import org.opencrow.app.ui.theme.LocalSpacing

@Composable
fun AttachmentPreviewRow(
    attachments: List<Attachment>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.sm, vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        items(attachments, key = { it.id }) { attachment ->
            AttachmentChip(attachment = attachment, onRemove = { onRemove(attachment.id) })
        }
    }
}

@Composable
private fun AttachmentChip(
    attachment: Attachment,
    onRemove: () -> Unit
) {
    val spacing = LocalSpacing.current

    Box(modifier = Modifier.size(72.dp)) {
        if (attachment.isImage) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(attachment.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = attachment.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(spacing.sm))
            )
        } else {
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomStart),
                shape = RoundedCornerShape(spacing.sm),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(spacing.xxs))
                    Text(
                        text = attachment.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopEnd)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove attachment",
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
