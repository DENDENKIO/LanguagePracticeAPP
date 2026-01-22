package com.example.languagepracticev3.ui.screens.routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.RouteDefinition
import com.example.languagepracticev3.viewmodel.RouteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    viewModel: RouteViewModel = hiltViewModel()
) {
    val builtInRoutes = viewModel.builtInRoutes
    val customRoutes by viewModel.customRoutes.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ルート (Routes)") },
                actions = {
                    IconButton(onClick = { viewModel.createNewRoute() }) {
                        Icon(Icons.Default.Add, "新規ルート")
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 左パネル: ルート一覧
            Card(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    item {
                        Text(
                            "組み込みルート",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    
                    items(builtInRoutes) { route ->
                        RouteListItem(
                            route = route,
                            isSelected = selectedRoute?.id == route.id,
                            onClick = { viewModel.selectRoute(route) },
                            onDelete = null
                        )
                    }
                    
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "カスタムルート",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    
                    items(customRoutes) { route ->
                        RouteListItem(
                            route = RouteDefinition(
                                id = route.id,
                                title = route.title,
                                description = route.description
                            ),
                            isSelected = selectedRoute?.id == route.id,
                            onClick = { viewModel.selectRouteFromCustom(route) },
                            onDelete = { viewModel.deleteRoute(route) }
                        )
                    }
                }
            }

            // 右パネル: ルート詳細
            Card(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
            ) {
                selectedRoute?.let { route ->
                    RouteDetail(
                        route = route,
                        isEditing = isEditing,
                        onEdit = { viewModel.startEditing() },
                        onCancel = { viewModel.cancelEditing() },
                        onSave = { title, description, steps ->
                            viewModel.saveRoute(title, description, steps)
                        }
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        "ルートを選択してください",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteListItem(
    route: RouteDefinition,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    ListItem(
        headlineContent = { Text(route.title) },
        supportingContent = route.description.takeIf { it.isNotEmpty() }?.let {
            { Text(it, maxLines = 1) }
        },
        leadingContent = {
            Icon(Icons.Default.Route, null)
        },
        trailingContent = onDelete?.let {
            {
                IconButton(onClick = it) {
                    Icon(Icons.Default.Delete, "削除")
                }
            }
        },
        colors = if (isSelected) {
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            ListItemDefaults.colors()
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RouteDetail(
    route: RouteDefinition,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onSave: (String, String, List<com.example.languagepracticev3.data.model.RouteStep>) -> Unit
) {
    var title by remember(route) { mutableStateOf(route.title) }
    var description by remember(route) { mutableStateOf(route.description) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("タイトル") },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    route.title,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            Row {
                if (isEditing) {
                    TextButton(onClick = onCancel) {
                        Text("キャンセル")
                    }
                    Button(onClick = { onSave(title, description, route.steps) }) {
                        Text("保存")
                    }
                } else {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "編集")
                    }
                }
            }
        }

        if (isEditing) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("説明") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                route.description.ifEmpty { "説明なし" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Divider()

        Text(
            "ステップ (${route.steps.size})",
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(route.steps) { step ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("${step.stepNumber}. ${step.title}") },
                        supportingContent = { Text(step.operation.name) },
                        leadingContent = {
                            Text(
                                "${step.stepNumber}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }
    }
}
