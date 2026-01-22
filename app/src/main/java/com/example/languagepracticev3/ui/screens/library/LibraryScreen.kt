// app/src/main/java/com/example/languagepracticev3/ui/screens/library/LibraryScreen.kt
package com.example.languagepracticev3.ui.screens.library

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.*
import com.example.languagepracticev3.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 既存ViewModelのStateFlowを収集
    val searchQuery by viewModel.searchQuery.collectAsState()
    val works by viewModel.searchResults.collectAsState()
    val studyCards by viewModel.studyCards.collectAsState()
    val personas by viewModel.personas.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val observations by viewModel.observations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val tabs = listOf("Works", "Cards", "Personas", "Topics", "Obs")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    var showSearchBar by remember { mutableStateOf(false) }
    var localSearchQuery by remember { mutableStateOf("") }

    // 編集ダイアログ用
    var showPersonaEditDialog by remember { mutableStateOf(false) }
    var showTopicEditDialog by remember { mutableStateOf(false) }
    var editingPersona by remember { mutableStateOf<Persona?>(null) }
    var editingTopic by remember { mutableStateOf<Topic?>(null) }

    // 詳細表示ダイアログ
    var showDetailDialog by remember { mutableStateOf(false) }
    var detailTitle by remember { mutableStateOf("") }
    var detailContent by remember { mutableStateOf("") }

    // 削除確認ダイアログ
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Any?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchBar) {
                        OutlinedTextField(
                            value = localSearchQuery,
                            onValueChange = {
                                localSearchQuery = it
                                viewModel.updateSearchQuery(it)
                            },
                            placeholder = { Text("検索...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                if (localSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        localSearchQuery = ""
                                        viewModel.updateSearchQuery("")
                                    }) {
                                        Icon(Icons.Default.Clear, "クリア")
                                    }
                                }
                            }
                        )
                    } else {
                        Text("ライブラリ")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSearchBar = !showSearchBar
                        if (!showSearchBar) {
                            localSearchQuery = ""
                            viewModel.updateSearchQuery("")
                        }
                    }) {
                        Icon(
                            if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                            "検索"
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "更新")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // タブ
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            val count = when(index) {
                                0 -> works.size
                                1 -> studyCards.size
                                2 -> personas.size
                                3 -> topics.size
                                4 -> observations.size
                                else -> 0
                            }
                            Text("$title ($count)")
                        }
                    )
                }
            }

            // ページャー
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> WorksList(
                        works = works,
                        onItemClick = { work ->
                            detailTitle = "Work: ${work.title ?: "(無題)"}"
                            detailContent = buildWorkDetail(work)
                            showDetailDialog = true
                        },
                        onDeleteClick = { work ->
                            deleteTarget = work
                            showDeleteDialog = true
                        }
                    )
                    1 -> StudyCardsList(
                        cards = studyCards,
                        onItemClick = { card ->
                            detailTitle = "StudyCard #${card.id}"
                            detailContent = buildStudyCardDetail(card)
                            showDetailDialog = true
                        },
                        onDeleteClick = { card ->
                            deleteTarget = card
                            showDeleteDialog = true
                        }
                    )
                    2 -> PersonasList(
                        personas = personas,
                        onItemClick = { persona ->
                            detailTitle = "Persona: ${persona.name}"
                            detailContent = buildPersonaDetail(persona)
                            showDetailDialog = true
                        },
                        onEditClick = { persona ->
                            editingPersona = persona
                            showPersonaEditDialog = true
                        },
                        onDeleteClick = { persona ->
                            deleteTarget = persona
                            showDeleteDialog = true
                        }
                    )
                    3 -> TopicsList(
                        topics = topics,
                        onItemClick = { topic ->
                            detailTitle = "Topic: ${topic.title}"
                            detailContent = buildTopicDetail(topic)
                            showDetailDialog = true
                        },
                        onEditClick = { topic ->
                            editingTopic = topic
                            showTopicEditDialog = true
                        },
                        onDeleteClick = { topic ->
                            deleteTarget = topic
                            showDeleteDialog = true
                        }
                    )
                    4 -> ObservationsList(
                        observations = observations,
                        onItemClick = { obs ->
                            detailTitle = "Observation: ${obs.motif}"
                            detailContent = buildObservationDetail(obs)
                            showDetailDialog = true
                        },
                        onDeleteClick = { obs ->
                            deleteTarget = obs
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    // Persona編集ダイアログ
    if (showPersonaEditDialog && editingPersona != null) {
        PersonaEditDialog(
            persona = editingPersona!!,
            onDismiss = {
                showPersonaEditDialog = false
                editingPersona = null
            },
            onSave = { updated ->
                viewModel.updatePersona(updated)
                showPersonaEditDialog = false
                editingPersona = null
                Toast.makeText(context, "ペルソナを更新しました", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Topic編集ダイアログ
    if (showTopicEditDialog && editingTopic != null) {
        TopicEditDialog(
            topic = editingTopic!!,
            onDismiss = {
                showTopicEditDialog = false
                editingTopic = null
            },
            onSave = { updated ->
                viewModel.updateTopic(updated)
                showTopicEditDialog = false
                editingTopic = null
                Toast.makeText(context, "トピックを更新しました", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 詳細表示ダイアログ
    if (showDetailDialog) {
        DetailDialog(
            title = detailTitle,
            content = detailContent,
            onDismiss = { showDetailDialog = false }
        )
    }

    // 削除確認ダイアログ
    if (showDeleteDialog && deleteTarget != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deleteTarget = null
            },
            title = { Text("削除確認") },
            text = { Text("この項目を削除しますか？\nこの操作は元に戻せません。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem(deleteTarget!!)
                        showDeleteDialog = false
                        deleteTarget = null
                        Toast.makeText(context, "削除しました", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleteTarget = null
                }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

// ==========================================
// リストコンポーネント
// ==========================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorksList(
    works: List<Work>,
    onItemClick: (Work) -> Unit,
    onDeleteClick: (Work) -> Unit
) {
    if (works.isEmpty()) {
        EmptyState("作品がありません")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(works, key = { it.id }) { work ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onItemClick(work) },
                            onLongClick = { onDeleteClick(work) }
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // ★修正: Null安全対応
                        Text(
                            text = work.title?.ifBlank { "(無題)" } ?: "(無題)",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        // ★修正: Null安全対応
                        Text(
                            text = (work.bodyText ?: "").take(100),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Row {
                            AssistChip(
                                onClick = {},
                                // ★修正: Null安全対応
                                label = { Text(work.kind.ifBlank { "UNKNOWN" }) },
                                modifier = Modifier.height(24.dp)
                            )
                            // ★修正: Null安全対応
                            val writerName = work.writerName
                            if (!writerName.isNullOrBlank()) {
                                Spacer(Modifier.width(4.dp))
                                AssistChip(
                                    onClick = {},
                                    label = { Text(writerName) },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StudyCardsList(
    cards: List<StudyCard>,
    onItemClick: (StudyCard) -> Unit,
    onDeleteClick: (StudyCard) -> Unit
) {
    if (cards.isEmpty()) {
        EmptyState("学習カードがありません")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards, key = { it.id }) { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onItemClick(card) },
                            onLongClick = { onDeleteClick(card) }
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // ★修正: Null安全対応
                        Text(
                            text = "Focus: ${card.focus ?: "(未設定)"}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        // ★修正: Null安全対応
                        Text(
                            text = "Level: ${card.level ?: "-"} | Tags: ${card.tags ?: "-"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PersonasList(
    personas: List<Persona>,
    onItemClick: (Persona) -> Unit,
    onEditClick: (Persona) -> Unit,
    onDeleteClick: (Persona) -> Unit
) {
    if (personas.isEmpty()) {
        EmptyState("ペルソナがありません")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(personas, key = { it.id }) { persona ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onItemClick(persona) },
                            onLongClick = { onDeleteClick(persona) }
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = persona.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = persona.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = persona.bio.take(80),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            val statusColor = when(persona.verificationStatus) {
                                "VERIFIED" -> MaterialTheme.colorScheme.primary
                                "PARTIALLY_VERIFIED" -> MaterialTheme.colorScheme.tertiary
                                "DISPUTED" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline
                            }
                            Text(
                                text = persona.verificationStatus.ifBlank { "UNVERIFIED" },
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor
                            )
                        }
                        IconButton(onClick = { onEditClick(persona) }) {
                            Icon(Icons.Default.Edit, "編集")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopicsList(
    topics: List<Topic>,
    onItemClick: (Topic) -> Unit,
    onEditClick: (Topic) -> Unit,
    onDeleteClick: (Topic) -> Unit
) {
    if (topics.isEmpty()) {
        EmptyState("トピックがありません")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(topics, key = { it.id }) { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onItemClick(topic) },
                            onLongClick = { onDeleteClick(topic) }
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Emotion: ${topic.emotion} | Scene: ${topic.scene}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (topic.tags.isNotBlank()) {
                                Text(
                                    text = topic.tags,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        IconButton(onClick = { onEditClick(topic) }) {
                            Icon(Icons.Default.Edit, "編集")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ObservationsList(
    observations: List<Observation>,
    onItemClick: (Observation) -> Unit,
    onDeleteClick: (Observation) -> Unit
) {
    if (observations.isEmpty()) {
        EmptyState("観察記録がありません")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(observations, key = { it.id }) { obs ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onItemClick(obs) },
                            onLongClick = { onDeleteClick(obs) }
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = obs.motif,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = obs.fullContent.take(100),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==========================================
// 編集ダイアログ
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonaEditDialog(
    persona: Persona,
    onDismiss: () -> Unit,
    onSave: (Persona) -> Unit
) {
    var name by remember { mutableStateOf(persona.name) }
    var location by remember { mutableStateOf(persona.location) }
    var bio by remember { mutableStateOf(persona.bio) }
    var style by remember { mutableStateOf(persona.style) }
    var tags by remember { mutableStateOf(persona.tags) }
    var verificationStatus by remember { mutableStateOf(persona.verificationStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ペルソナ編集") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名前") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("場所/国籍") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("経歴・概要") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                OutlinedTextField(
                    value = style,
                    onValueChange = { style = it },
                    label = { Text("文体・特徴") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("タグ（カンマ区切り）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // 検証ステータス選択
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = verificationStatus.ifBlank { "UNVERIFIED" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("検証ステータス") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("UNVERIFIED", "PARTIALLY_VERIFIED", "VERIFIED", "DISPUTED").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    verificationStatus = status
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(persona.copy(
                    name = name,
                    location = location,
                    bio = bio,
                    style = style,
                    tags = tags,
                    verificationStatus = verificationStatus
                ))
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicEditDialog(
    topic: Topic,
    onDismiss: () -> Unit,
    onSave: (Topic) -> Unit
) {
    var title by remember { mutableStateOf(topic.title) }
    var emotion by remember { mutableStateOf(topic.emotion) }
    var scene by remember { mutableStateOf(topic.scene) }
    var tags by remember { mutableStateOf(topic.tags) }
    var fixConditions by remember { mutableStateOf(topic.fixConditions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("トピック編集") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("タイトル") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = emotion,
                    onValueChange = { emotion = it },
                    label = { Text("感情") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Scene選択
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = scene.ifBlank { "STATIC" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("シーン") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("STATIC", "DYNAMIC", "MIX").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    scene = s
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("タグ（カンマ区切り）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = fixConditions,
                    onValueChange = { fixConditions = it },
                    label = { Text("固定条件") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(topic.copy(
                    title = title,
                    emotion = emotion,
                    scene = scene,
                    tags = tags,
                    fixConditions = fixConditions
                ))
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
private fun DetailDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(content, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

// ==========================================
// ヘルパー（★Null安全対応）
// ==========================================

private fun buildWorkDetail(work: Work): String = """
Kind: ${work.kind}
Writer: ${work.writerName ?: "-"}
Reader: ${work.readerNote ?: "-"}
Tone: ${work.toneLabel ?: "-"}
Created: ${work.createdAt ?: "-"}

--- 本文 ---
${work.bodyText ?: "(本文なし)"}
""".trimIndent()

private fun buildStudyCardDetail(card: StudyCard): String = """
Focus: ${card.focus ?: "-"}
Level: ${card.level ?: "-"}
Tags: ${card.tags ?: "-"}

--- Best Expressions ---
${card.bestExpressionsRaw ?: "(なし)"}

--- Metaphors ---
${card.metaphorChainsRaw ?: "(なし)"}

--- Do Next ---
${card.doNextRaw ?: "(なし)"}

--- Full Parse ---
${card.fullParsedContent ?: "(なし)"}
""".trimIndent()

private fun buildPersonaDetail(persona: Persona): String = """
Name: ${persona.name}
Location: ${persona.location}
Status: ${persona.verificationStatus.ifBlank { "UNVERIFIED" }}
Tags: ${persona.tags}
Created: ${persona.createdAt}

--- Bio ---
${persona.bio}

--- Style ---
${persona.style}
""".trimIndent()

private fun buildTopicDetail(topic: Topic): String = """
Title: ${topic.title}
Emotion: ${topic.emotion}
Scene: ${topic.scene}
Tags: ${topic.tags}
Created: ${topic.createdAt}

--- Fix Conditions ---
${topic.fixConditions}
""".trimIndent()

private fun buildObservationDetail(obs: Observation): String = """
Motif: ${obs.motif}
Image: ${obs.imageUrl}

--- Visual ---
${obs.visualRaw}

--- Sound ---
${obs.soundRaw}

--- Metaphors ---
${obs.metaphorsRaw}

--- Core Candidates ---
${obs.coreCandidatesRaw}

--- Full Content ---
${obs.fullContent}
""".trimIndent()
