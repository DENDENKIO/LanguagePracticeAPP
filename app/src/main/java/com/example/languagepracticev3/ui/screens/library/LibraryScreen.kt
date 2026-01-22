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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.*
import com.example.languagepracticev3.viewmodel.LibraryTab
import com.example.languagepracticev3.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // ViewModelのStateFlowを収集
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

    // ★編集ダイアログ用（全タイプ対応）
    var showWorkEditDialog by remember { mutableStateOf(false) }
    var showStudyCardEditDialog by remember { mutableStateOf(false) }
    var showPersonaEditDialog by remember { mutableStateOf(false) }
    var showTopicEditDialog by remember { mutableStateOf(false) }
    var showObservationEditDialog by remember { mutableStateOf(false) }

    var editingWork by remember { mutableStateOf<Work?>(null) }
    var editingStudyCard by remember { mutableStateOf<StudyCard?>(null) }
    var editingPersona by remember { mutableStateOf<Persona?>(null) }
    var editingTopic by remember { mutableStateOf<Topic?>(null) }
    var editingObservation by remember { mutableStateOf<Observation?>(null) }

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
                        onEditClick = { work ->
                            editingWork = work
                            showWorkEditDialog = true
                        },
                        onDeleteClick = { work ->
                            deleteTarget = work
                            showDeleteDialog = true
                        },
                        onCopyClick = { work ->
                            clipboardManager.setText(AnnotatedString(work.bodyText ?: ""))
                            Toast.makeText(context, "本文をコピーしました", Toast.LENGTH_SHORT).show()
                        }
                    )
                    1 -> StudyCardsList(
                        cards = studyCards,
                        onItemClick = { card ->
                            detailTitle = "StudyCard #${card.id}"
                            detailContent = buildStudyCardDetail(card)
                            showDetailDialog = true
                        },
                        onEditClick = { card ->
                            editingStudyCard = card
                            showStudyCardEditDialog = true
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
                        onEditClick = { obs ->
                            editingObservation = obs
                            showObservationEditDialog = true
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

    // ★Work編集ダイアログ
    if (showWorkEditDialog && editingWork != null) {
        WorkEditDialog(
            work = editingWork!!,
            onDismiss = {
                showWorkEditDialog = false
                editingWork = null
            },
            onSave = { updated ->
                viewModel.updateWork(updated)
                showWorkEditDialog = false
                editingWork = null
                Toast.makeText(context, "作品を更新しました", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ★StudyCard編集ダイアログ
    if (showStudyCardEditDialog && editingStudyCard != null) {
        StudyCardEditDialog(
            card = editingStudyCard!!,
            onDismiss = {
                showStudyCardEditDialog = false
                editingStudyCard = null
            },
            onSave = { updated ->
                viewModel.updateStudyCard(updated)
                showStudyCardEditDialog = false
                editingStudyCard = null
                Toast.makeText(context, "学習カードを更新しました", Toast.LENGTH_SHORT).show()
            }
        )
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

    // ★Observation編集ダイアログ
    if (showObservationEditDialog && editingObservation != null) {
        ObservationEditDialog(
            observation = editingObservation!!,
            onDismiss = {
                showObservationEditDialog = false
                editingObservation = null
            },
            onSave = { updated ->
                viewModel.updateObservation(updated)
                showObservationEditDialog = false
                editingObservation = null
                Toast.makeText(context, "観察記録を更新しました", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 詳細表示ダイアログ
    if (showDetailDialog) {
        DetailDialog(
            title = detailTitle,
            content = detailContent,
            onDismiss = { showDetailDialog = false },
            onCopy = {
                clipboardManager.setText(AnnotatedString(detailContent))
                Toast.makeText(context, "内容をコピーしました", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 削除確認ダイアログ
    if (showDeleteDialog && deleteTarget != null) {
        val targetName = when (val t = deleteTarget) {
            is Work -> "作品「${t.title ?: "(無題)"}」"
            is StudyCard -> "学習カード #${t.id}"
            is Persona -> "ペルソナ「${t.name}」"
            is Topic -> "トピック「${t.title}」"
            is Observation -> "観察記録「${t.motif}」"
            else -> "この項目"
        }

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deleteTarget = null
            },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("削除確認") },
            text = { Text("${targetName}を削除しますか？\nこの操作は元に戻せません。") },
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
// リストコンポーネント（編集ボタン追加）
// ==========================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorksList(
    works: List<Work>,
    onItemClick: (Work) -> Unit,
    onEditClick: (Work) -> Unit,
    onDeleteClick: (Work) -> Unit,
    onCopyClick: (Work) -> Unit
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
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = work.title?.ifBlank { "(無題)" } ?: "(無題)",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = (work.bodyText ?: "").take(100),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(work.kind.ifBlank { "UNKNOWN" }) },
                                    modifier = Modifier.height(24.dp)
                                )
                                val writerName = work.writerName
                                if (!writerName.isNullOrBlank()) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(writerName) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }
                        }
                        Column {
                            IconButton(onClick = { onEditClick(work) }) {
                                Icon(Icons.Default.Edit, "編集", modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { onCopyClick(work) }) {
                                Icon(Icons.Default.ContentCopy, "コピー", modifier = Modifier.size(20.dp))
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
    onEditClick: (StudyCard) -> Unit,
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
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Focus: ${card.focus ?: "(未設定)"}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Level: ${card.level ?: "-"} | Tags: ${card.tags ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onEditClick(card) }) {
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
    onEditClick: (Observation) -> Unit,
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
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
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
                        IconButton(onClick = { onEditClick(obs) }) {
                            Icon(Icons.Default.Edit, "編集")
                        }
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// 編集ダイアログ
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkEditDialog(
    work: Work,
    onDismiss: () -> Unit,
    onSave: (Work) -> Unit
) {
    var title by remember { mutableStateOf(work.title ?: "") }
    var bodyText by remember { mutableStateOf(work.bodyText ?: "") }
    var writerName by remember { mutableStateOf(work.writerName ?: "") }
    var readerNote by remember { mutableStateOf(work.readerNote ?: "") }
    var toneLabel by remember { mutableStateOf(work.toneLabel ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("作品を編集") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
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
                    value = writerName,
                    onValueChange = { writerName = it },
                    label = { Text("書き手") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = readerNote,
                    onValueChange = { readerNote = it },
                    label = { Text("読者ノート") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = toneLabel,
                    onValueChange = { toneLabel = it },
                    label = { Text("トーン") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bodyText,
                    onValueChange = { bodyText = it },
                    label = { Text("本文") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 20
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(work.copy(
                    title = title.ifBlank { null },
                    bodyText = bodyText.ifBlank { null },
                    writerName = writerName.ifBlank { null },
                    readerNote = readerNote.ifBlank { null },
                    toneLabel = toneLabel.ifBlank { null }
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
private fun StudyCardEditDialog(
    card: StudyCard,
    onDismiss: () -> Unit,
    onSave: (StudyCard) -> Unit
) {
    var focus by remember { mutableStateOf(card.focus ?: "") }
    var level by remember { mutableStateOf(card.level ?: "NORMAL") }
    var bestExpressions by remember { mutableStateOf(card.bestExpressionsRaw ?: "") }
    var metaphorChains by remember { mutableStateOf(card.metaphorChainsRaw ?: "") }
    var doNext by remember { mutableStateOf(card.doNextRaw ?: "") }
    var tags by remember { mutableStateOf(card.tags ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("学習カードを編集") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = focus,
                    onValueChange = { focus = it },
                    label = { Text("Focus") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Level選択
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = level,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("NORMAL", "INTERMEDIATE", "ADVANCED").forEach { l ->
                            DropdownMenuItem(
                                text = { Text(l) },
                                onClick = {
                                    level = l
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
                    value = bestExpressions,
                    onValueChange = { bestExpressions = it },
                    label = { Text("Best Expressions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )
                OutlinedTextField(
                    value = metaphorChains,
                    onValueChange = { metaphorChains = it },
                    label = { Text("Metaphor Chains") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )
                OutlinedTextField(
                    value = doNext,
                    onValueChange = { doNext = it },
                    label = { Text("Do Next") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(card.copy(
                    focus = focus.ifBlank { null },
                    level = level.ifBlank { null },
                    bestExpressionsRaw = bestExpressions.ifBlank { null },
                    metaphorChainsRaw = metaphorChains.ifBlank { null },
                    doNextRaw = doNext.ifBlank { null },
                    tags = tags.ifBlank { null }
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
        title = { Text("ペルソナを編集") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
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
        title = { Text("トピックを編集") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ObservationEditDialog(
    observation: Observation,
    onDismiss: () -> Unit,
    onSave: (Observation) -> Unit
) {
    var motif by remember { mutableStateOf(observation.motif) }
    var imageUrl by remember { mutableStateOf(observation.imageUrl) }
    var visualRaw by remember { mutableStateOf(observation.visualRaw) }
    var soundRaw by remember { mutableStateOf(observation.soundRaw) }
    var metaphorsRaw by remember { mutableStateOf(observation.metaphorsRaw) }
    var coreCandidatesRaw by remember { mutableStateOf(observation.coreCandidatesRaw) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("観察記録を編集") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = motif,
                    onValueChange = { motif = it },
                    label = { Text("モチーフ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("画像URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = visualRaw,
                    onValueChange = { visualRaw = it },
                    label = { Text("Visual（視覚）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                OutlinedTextField(
                    value = soundRaw,
                    onValueChange = { soundRaw = it },
                    label = { Text("Sound（音）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                OutlinedTextField(
                    value = metaphorsRaw,
                    onValueChange = { metaphorsRaw = it },
                    label = { Text("Metaphors（比喩）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                OutlinedTextField(
                    value = coreCandidatesRaw,
                    onValueChange = { coreCandidatesRaw = it },
                    label = { Text("Core Candidates（核候補）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(observation.copy(
                    motif = motif,
                    imageUrl = imageUrl,
                    visualRaw = visualRaw,
                    soundRaw = soundRaw,
                    metaphorsRaw = metaphorsRaw,
                    coreCandidatesRaw = coreCandidatesRaw
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
    onDismiss: () -> Unit,
    onCopy: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, "コピー")
                }
            }
        },
        text = {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(content, style = MaterialTheme.typography.bodySmall)
                }
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
// ヘルパー（Null安全対応）
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
