package org.opencrow.app.ui.screens.settings.panels

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.opencrow.app.data.remote.dto.*
import org.opencrow.app.ui.components.InfoTooltip
import org.opencrow.app.ui.theme.LocalSpacing

// ─── Shared helpers ───

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun AddButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun RemoveButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun Field(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, keyboard: KeyboardType = KeyboardType.Text, isPassword: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodySmall,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}


// ─── Email ───

@Composable
fun EmailConfigPanel(cfg: UserConfigDto, onChange: (UserConfigDto) -> Unit) {
    val spacing = LocalSpacing.current
    val accounts = cfg.integrations?.emailAccounts.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SectionTitle("Email Accounts")
        accounts.forEachIndexed { i, acct ->
            var expanded by remember { mutableStateOf(false) }
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).let { if (!expanded) it else it }) {
                            Text(acct.label.ifBlank { "Account ${i + 1}" }, style = MaterialTheme.typography.titleSmall)
                            if (!expanded) Text(acct.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = acct.enabled, onCheckedChange = { en ->
                            val updated = accounts.toMutableList()
                            updated[i] = acct.copy(enabled = en)
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(emailAccounts = updated)))
                        })
                        RemoveButton {
                            val updated = accounts.toMutableList().apply { removeAt(i) }
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(emailAccounts = updated)))
                        }
                    }
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Collapse" else "Edit", style = MaterialTheme.typography.labelSmall)
                    }
                    if (expanded) {
                        Spacer(Modifier.height(spacing.sm))
                        val update = { new: EmailAccountDto ->
                            val updated = accounts.toMutableList(); updated[i] = new
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(emailAccounts = updated)))
                        }
                        Field("Label", acct.label, { update(acct.copy(label = it)) })
                        Spacer(Modifier.height(spacing.xs))
                        Field("Address", acct.address, { update(acct.copy(address = it)) }, keyboard = KeyboardType.Email)
                        Spacer(Modifier.height(spacing.xs))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            Field("IMAP Host", acct.imapHost, { update(acct.copy(imapHost = it)) }, Modifier.weight(1f))
                            Field("Port", acct.imapPort.toString(), { update(acct.copy(imapPort = it.toIntOrNull() ?: 993)) }, Modifier.width(80.dp), KeyboardType.Number)
                        }
                        Spacer(Modifier.height(spacing.xs))
                        Field("IMAP Username", acct.imapUsername, { update(acct.copy(imapUsername = it)) })
                        Spacer(Modifier.height(spacing.xs))
                        Field("IMAP Password", acct.imapPassword.orEmpty(), { update(acct.copy(imapPassword = it)) }, isPassword = true)
                        Spacer(Modifier.height(spacing.xs))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            Field("SMTP Host", acct.smtpHost, { update(acct.copy(smtpHost = it)) }, Modifier.weight(1f))
                            Field("Port", acct.smtpPort.toString(), { update(acct.copy(smtpPort = it.toIntOrNull() ?: 587)) }, Modifier.width(80.dp), KeyboardType.Number)
                        }
                        Spacer(Modifier.height(spacing.xs))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("TLS", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.width(spacing.sm))
                            Switch(checked = acct.tls, onCheckedChange = { update(acct.copy(tls = it)) })
                            Spacer(Modifier.weight(1f))
                            Field("Poll (s)", (acct.pollIntervalSeconds ?: 900).toString(), { update(acct.copy(pollIntervalSeconds = it.toIntOrNull() ?: 900)) }, Modifier.width(100.dp), KeyboardType.Number)
                        }
                    }
                }
            }
        }
        AddButton("Add Email Account") {
            val newAcct = EmailAccountDto(null, "", "", "", 993, "", null, "", 587, true, true, 900)
            val updated = accounts + newAcct
            onChange(cfg.copy(integrations = (cfg.integrations ?: IntegrationsDto(null, null, null, null, null)).copy(emailAccounts = updated)))
        }
    }
}


// ─── Telegram Bots ───

@Composable
fun ChannelsConfigPanel(cfg: UserConfigDto, onChange: (UserConfigDto) -> Unit) {
    val spacing = LocalSpacing.current
    val bots = cfg.integrations?.telegramBots.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SectionTitle("Telegram Bots")
        bots.forEachIndexed { i, bot ->
            var expanded by remember { mutableStateOf(false) }
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(bot.label.ifBlank { "Bot ${i + 1}" }, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Switch(checked = bot.enabled, onCheckedChange = { en ->
                            val updated = bots.toMutableList(); updated[i] = bot.copy(enabled = en)
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(telegramBots = updated)))
                        })
                        RemoveButton {
                            val updated = bots.toMutableList().apply { removeAt(i) }
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(telegramBots = updated)))
                        }
                    }
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Collapse" else "Edit", style = MaterialTheme.typography.labelSmall)
                    }
                    if (expanded) {
                        val update = { new: TelegramBotDto ->
                            val updated = bots.toMutableList(); updated[i] = new
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(telegramBots = updated)))
                        }
                        Field("Label", bot.label, { update(bot.copy(label = it)) })
                        Spacer(Modifier.height(spacing.xs))
                        Field("Bot Token", bot.botToken.orEmpty(), { update(bot.copy(botToken = it)) }, isPassword = true)
                        Spacer(Modifier.height(spacing.xs))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Field("Notification Chat ID", bot.notificationChatId.orEmpty(), { update(bot.copy(notificationChatId = it)) }, modifier = Modifier.weight(1f))
                            InfoTooltip("You can find your chat ID on your Telegram profile page — it's called ID and it's a 9-digit number.")
                        }
                        Spacer(Modifier.height(spacing.xs))
                        Field("Allowed Chat IDs (comma-separated)", bot.allowedChatIds?.joinToString(", ").orEmpty(), { v ->
                            update(bot.copy(allowedChatIds = v.split(",").map { it.trim() }.filter { it.isNotEmpty() }))
                        })
                        Spacer(Modifier.height(spacing.xs))
                        Field("Poll Interval (s)", (bot.pollIntervalSeconds ?: 5).toString(), { update(bot.copy(pollIntervalSeconds = it.toIntOrNull() ?: 5)) }, keyboard = KeyboardType.Number)
                    }
                }
            }
        }
        AddButton("Add Telegram Bot") {
            val newBot = TelegramBotDto(null, "", null, emptyList(), null, true, 5)
            val updated = bots + newBot
            onChange(cfg.copy(integrations = (cfg.integrations ?: IntegrationsDto(null, null, null, null, null)).copy(telegramBots = updated)))
        }
    }
}

// ─── Devices / Companion Apps ───

@Composable
fun DevicesConfigPanel(cfg: UserConfigDto, onChange: (UserConfigDto) -> Unit) {
    val spacing = LocalSpacing.current
    val apps = cfg.integrations?.companionApps.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SectionTitle("Companion Apps")
        apps.forEachIndexed { i, app ->
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.label ?: app.name, style = MaterialTheme.typography.titleSmall)
                            Text("ID: ${app.id ?: "—"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = app.enabled, onCheckedChange = { en ->
                            val updated = apps.toMutableList(); updated[i] = app.copy(enabled = en)
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(companionApps = updated)))
                        })
                        RemoveButton {
                            val updated = apps.toMutableList().apply { removeAt(i) }
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(companionApps = updated)))
                        }
                    }
                    Spacer(Modifier.height(spacing.sm))
                    Field("Display Name", app.label.orEmpty(), { v ->
                        val updated = apps.toMutableList(); updated[i] = app.copy(label = v)
                        onChange(cfg.copy(integrations = cfg.integrations?.copy(companionApps = updated)))
                    })
                    Spacer(Modifier.height(spacing.xs))
                    Field("Identifier", app.name, { v ->
                        val updated = apps.toMutableList(); updated[i] = app.copy(name = v)
                        onChange(cfg.copy(integrations = cfg.integrations?.copy(companionApps = updated)))
                    })
                }
            }
        }
        AddButton("Add Companion App") {
            val newApp = CompanionAppDto(null, "", "", true)
            val updated = apps + newApp
            onChange(cfg.copy(integrations = (cfg.integrations ?: IntegrationsDto(null, null, null, null, null)).copy(companionApps = updated)))
        }
    }
}


// ─── SSH Servers ───

@Composable
fun ServersConfigPanel(cfg: UserConfigDto, onChange: (UserConfigDto) -> Unit) {
    val spacing = LocalSpacing.current
    val servers = cfg.integrations?.sshServers.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SectionTitle("SSH Servers")
        servers.forEachIndexed { i, srv ->
            var expanded by remember { mutableStateOf(false) }
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(srv.name.ifBlank { "Server ${i + 1}" }, style = MaterialTheme.typography.titleSmall)
                            if (!expanded) Text("${srv.username}@${srv.host}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = srv.enabled, onCheckedChange = { en ->
                            val updated = servers.toMutableList(); updated[i] = srv.copy(enabled = en)
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(sshServers = updated)))
                        })
                        RemoveButton {
                            val updated = servers.toMutableList().apply { removeAt(i) }
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(sshServers = updated)))
                        }
                    }
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Collapse" else "Edit", style = MaterialTheme.typography.labelSmall)
                    }
                    if (expanded) {
                        val update = { new: SshServerDto ->
                            val updated = servers.toMutableList(); updated[i] = new
                            onChange(cfg.copy(integrations = cfg.integrations?.copy(sshServers = updated)))
                        }
                        Field("Server Name", srv.name, { update(srv.copy(name = it)) })
                        Spacer(Modifier.height(spacing.xs))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            Field("Host", srv.host, { update(srv.copy(host = it)) }, Modifier.weight(1f))
                            Field("Port", (srv.port ?: 22).toString(), { update(srv.copy(port = it.toIntOrNull() ?: 22)) }, Modifier.width(80.dp), KeyboardType.Number)
                        }
                        Spacer(Modifier.height(spacing.xs))
                        Field("Username", srv.username, { update(srv.copy(username = it)) })
                        Spacer(Modifier.height(spacing.xs))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Auth: ", style = MaterialTheme.typography.bodySmall)
                            FilterChip(selected = srv.authMode == "key", onClick = { update(srv.copy(authMode = "key")) }, label = { Text("Key") }, shape = MaterialTheme.shapes.extraSmall)
                            Spacer(Modifier.width(spacing.sm))
                            FilterChip(selected = srv.authMode == "password", onClick = { update(srv.copy(authMode = "password")) }, label = { Text("Password") }, shape = MaterialTheme.shapes.extraSmall)
                        }
                    }
                }
            }
        }
        AddButton("Add SSH Server") {
            val newSrv = SshServerDto(null, "", "", 22, "", "key", true)
            val updated = servers + newSrv
            onChange(cfg.copy(integrations = (cfg.integrations ?: IntegrationsDto(null, null, null, null, null)).copy(sshServers = updated)))
        }
    }
}

// ─── Tools ───

@Composable
fun ToolsConfigPanel(cfg: UserConfigDto, onChange: (UserConfigDto) -> Unit) {
    val spacing = LocalSpacing.current
    val defs = cfg.tools?.definitions.orEmpty()
    val enabled = cfg.tools?.enabled.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SectionTitle("Tools")
        if (defs.isEmpty()) {
            Text("No tools configured.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        defs.forEachIndexed { i, tool ->
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium) {
                Row(modifier = Modifier.padding(spacing.md), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tool.name, style = MaterialTheme.typography.titleSmall)
                        Text(tool.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                    Switch(
                        checked = enabled[tool.id] ?: true,
                        onCheckedChange = { en ->
                            val updatedEnabled = enabled.toMutableMap()
                            tool.id?.let { updatedEnabled[it] = en }
                            onChange(cfg.copy(tools = cfg.tools?.copy(enabled = updatedEnabled)))
                        }
                    )
                }
            }
        }
    }
}


// ─── Skills ───

@Composable
fun SkillsConfigPanel(cfg: UserConfigDto, onChange: (UserConfigDto) -> Unit) {
    val spacing = LocalSpacing.current
    val entries = cfg.skills?.entries.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SectionTitle("Skills")
        entries.forEachIndexed { i, skill ->
            var expanded by remember { mutableStateOf(false) }
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(skill.name.ifBlank { "Skill ${i + 1}" }, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Switch(checked = skill.enabled, onCheckedChange = { en ->
                            val updated = entries.toMutableList(); updated[i] = skill.copy(enabled = en)
                            onChange(cfg.copy(skills = cfg.skills?.copy(entries = updated)))
                        })
                        RemoveButton {
                            val updated = entries.toMutableList().apply { removeAt(i) }
                            onChange(cfg.copy(skills = cfg.skills?.copy(entries = updated)))
                        }
                    }
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Collapse" else "Edit", style = MaterialTheme.typography.labelSmall)
                    }
                    if (expanded) {
                        val update = { new: SkillEntryDto ->
                            val updated = entries.toMutableList(); updated[i] = new
                            onChange(cfg.copy(skills = cfg.skills?.copy(entries = updated)))
                        }
                        Field("Name", skill.name, { update(skill.copy(name = it)) })
                        Spacer(Modifier.height(spacing.xs))
                        Field("Description", skill.description, { update(skill.copy(description = it)) })
                        Spacer(Modifier.height(spacing.xs))
                        OutlinedTextField(
                            value = skill.content.orEmpty(),
                            onValueChange = { update(skill.copy(content = it)) },
                            label = { Text("Content", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            maxLines = 10
                        )
                    }
                }
            }
        }
        AddButton("Add Skill") {
            val newSkill = SkillEntryDto(null, "", "", "", true)
            val updated = entries + newSkill
            onChange(cfg.copy(skills = (cfg.skills ?: SkillsDto(null)).copy(entries = updated)))
        }
    }
}

// ─── MCP Servers ───

@Composable
fun McpConfigPanel(cfg: UserConfigDto, onChange: (UserConfigDto) -> Unit) {
    val spacing = LocalSpacing.current
    val servers = cfg.mcp?.servers.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SectionTitle("MCP Servers")
        servers.forEachIndexed { i, srv ->
            var expanded by remember { mutableStateOf(false) }
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(srv.name.ifBlank { "MCP ${i + 1}" }, style = MaterialTheme.typography.titleSmall)
                            if (!expanded) Text(srv.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        Switch(checked = srv.enabled, onCheckedChange = { en ->
                            val updated = servers.toMutableList(); updated[i] = srv.copy(enabled = en)
                            onChange(cfg.copy(mcp = cfg.mcp?.copy(servers = updated)))
                        })
                        RemoveButton {
                            val updated = servers.toMutableList().apply { removeAt(i) }
                            onChange(cfg.copy(mcp = cfg.mcp?.copy(servers = updated)))
                        }
                    }
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Collapse" else "Edit", style = MaterialTheme.typography.labelSmall)
                    }
                    if (expanded) {
                        val update = { new: McpServerDto ->
                            val updated = servers.toMutableList(); updated[i] = new
                            onChange(cfg.copy(mcp = cfg.mcp?.copy(servers = updated)))
                        }
                        Field("Name", srv.name, { update(srv.copy(name = it)) })
                        Spacer(Modifier.height(spacing.xs))
                        Field("URL", srv.url, { update(srv.copy(url = it)) })
                    }
                }
            }
        }
        AddButton("Add MCP Server") {
            val newSrv = McpServerDto(null, "", "", null, true)
            val updated = servers + newSrv
            onChange(cfg.copy(mcp = (cfg.mcp ?: McpDto(null)).copy(servers = updated)))
        }
    }
}

// ─── Schedules ───

@Composable
fun SchedulesConfigPanel(cfg: UserConfigDto, onChange: (UserConfigDto) -> Unit) {
    val spacing = LocalSpacing.current
    val entries = cfg.schedules?.entries.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SectionTitle("Schedules")
        entries.forEachIndexed { i, sched ->
            var expanded by remember { mutableStateOf(false) }
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sched.description.ifBlank { "Schedule ${i + 1}" }, style = MaterialTheme.typography.titleSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                                sched.status?.let {
                                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                sched.cronExpression?.let {
                                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        RemoveButton {
                            val updated = entries.toMutableList().apply { removeAt(i) }
                            onChange(cfg.copy(schedules = cfg.schedules?.copy(entries = updated)))
                        }
                    }
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Collapse" else "Edit", style = MaterialTheme.typography.labelSmall)
                    }
                    if (expanded) {
                        val update = { new: ScheduleEntryDto ->
                            val updated = entries.toMutableList(); updated[i] = new
                            onChange(cfg.copy(schedules = cfg.schedules?.copy(entries = updated)))
                        }
                        Field("Description", sched.description, { update(sched.copy(description = it)) })
                        Spacer(Modifier.height(spacing.xs))
                        Field("Execute At", sched.executeAt.orEmpty(), { update(sched.copy(executeAt = it)) })
                        Spacer(Modifier.height(spacing.xs))
                        Field("Cron Expression", sched.cronExpression.orEmpty(), { update(sched.copy(cronExpression = it.ifBlank { null })) })
                        Spacer(Modifier.height(spacing.xs))
                        OutlinedTextField(
                            value = sched.prompt.orEmpty(),
                            onValueChange = { update(sched.copy(prompt = it)) },
                            label = { Text("Prompt", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            maxLines = 5
                        )
                    }
                }
            }
        }
        AddButton("Add Schedule") {
            val newSched = ScheduleEntryDto(null, "", "PENDING", "", null, "")
            val updated = entries + newSched
            onChange(cfg.copy(schedules = (cfg.schedules ?: SchedulesDto(null)).copy(entries = updated)))
        }
    }
}
