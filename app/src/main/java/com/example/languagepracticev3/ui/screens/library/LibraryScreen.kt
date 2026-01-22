package com.example.languagepracticev3.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.Work
import com.example.languagepracticev3.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val studyCards by viewModel.studyCards.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedWork by viewModel.selectedWork.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ライブラリ (Library)") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 検索バー
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("検索...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            // タブ
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("作品 (${searchResults.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("学習カード (${studyCards.size})") }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 左パネル: リスト
                Card(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                ) {
                    when (selectedTab) {
                        0 -> WorksList(
                            works = searchResults,
                            selectedWork = selectedWork,
                            onSelect = viewModel::selectWork,
                            onDelete = viewModel::deleteWork
                        )
                        1 -> StudyCardsList(
                            cards = studyCards,
                            onDelete = viewModel::deleteStudyCard
                        )
                    }
                }

                // 右パネル: 詳細
                Card(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                ) {
                    selectedWork?.let { work ->
                        WorkDetail(work = work)
                    } ?: Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            "作品を選択してください",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorksList(
    works: List<Work>,
    selectedWork: Work?,
    onSelect: (Work?) -> Unit,
    onDelete: (Work) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(works) { work ->
            ListItem(
                headlineContent = { Text(work.title?.ifEmpty { "無題" } ?: "無題") },
                supportingContent = {
                    Text(
                        (work.bodyText ?: "").take(50),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    Icon(
                        when (work.kind) {
                            "GIKO" -> Icons.Default.AutoAwesome
                            "POETRY" -> Icons.Default.TheaterComedy
                            else -> Icons.Default.Article
                        },
                        null
                    )
                },
                trailingContent = {
                    IconButton(onClick = { onDelete(work) }) {
                        Icon(Icons.Default.Delete, "削除")
                    }
                },
                colors = if (selectedWork?.id == work.id) {
                    ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                } else {
                    ListItemDefaults.colors()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StudyCardsList(
    cards: List<com.example.languagepracticev3.data.model.StudyCard>,
    onDelete: (com.example.languagepracticev3.data.model.StudyCard) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(cards) { card ->
            ListItem(
                headlineContent = { Text(card.focus?.ifEmpty { "学習カード #${card.id}" } ?: "学習カード #${card.id}") },
                supportingContent = {
                    Text(
                        "レベル: ${card.level?.ifEmpty { "未設定" } ?: "未設定"}",
                        maxLines = 1
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.School, null)
                },
                trailingContent = {
                    IconButton(onClick = { onDelete(card) }) {
                        Icon(Icons.Default.Delete, "削除")
                    }
                }
            )
        }
    }
}

@Composable
private fun WorkDetail(work: Work) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            work.title?.ifEmpty { "無題" } ?: "無題",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {},
                label = { Text(work.kind.ifEmpty { "TEXT_GEN" }) }
            )
            work.writerName?.takeIf { it.isNotEmpty() }?.let {
                AssistChip(onClick = {}, label = { Text(it) })
            }
        }

        HorizontalDivider()

        Text(
            "本文",
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            work.bodyText ?: "",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        work.readerNote?.takeIf { it.isNotEmpty() }?.let {
            HorizontalDivider()
            Text(
                "読者像メモ",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            "作成日: ${work.createdAt ?: "不明"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
