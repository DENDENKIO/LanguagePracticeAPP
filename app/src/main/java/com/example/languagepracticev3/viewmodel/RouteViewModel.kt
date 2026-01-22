package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.CustomRouteDao
import com.example.languagepracticev3.data.model.CustomRoute
import com.example.languagepracticev3.data.model.RouteDefinition
import com.example.languagepracticev3.data.model.RouteStep
import com.example.languagepracticev3.data.model.OperationKind
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val customRouteDao: CustomRouteDao
) : ViewModel() {

    private val gson = Gson()

    // ★修正: getAll() → observeAll() に変更（FlowをStateFlowに変換）
    val customRoutes: StateFlow<List<CustomRoute>> = customRouteDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedRoute = MutableStateFlow<RouteDefinition?>(null)
    val selectedRoute: StateFlow<RouteDefinition?> = _selectedRoute.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    val builtInRoutes: List<RouteDefinition> = listOf(
        RouteDefinition(
            id = "basic_text_gen",
            title = "基本テキスト生成",
            description = "テーマから文章を生成する基本ルート",
            steps = listOf(
                RouteStep(1, "テーマ入力", OperationKind.TEXT_GEN),
                RouteStep(2, "文章生成", OperationKind.TEXT_GEN)
            )
        ),
        RouteDefinition(
            id = "giko_route",
            title = "技巧ルート",
            description = "比喩や表現技法を駆使した文章生成",
            steps = listOf(
                RouteStep(1, "テーマ設定", OperationKind.TEXT_GEN),
                RouteStep(2, "技巧適用", OperationKind.GIKO),
                RouteStep(3, "出力", OperationKind.TEXT_GEN)
            )
        ),
        RouteDefinition(
            id = "poetry_route",
            title = "詩作ルート",
            description = "詩を作成するためのルート",
            steps = listOf(
                RouteStep(1, "テーマ・感情設定", OperationKind.TEXT_GEN),
                RouteStep(2, "ドラフト作成", OperationKind.POETRY_DRAFT),
                RouteStep(3, "コア抽出", OperationKind.POETRY_CORE),
                RouteStep(4, "推敲", OperationKind.POETRY_REV)
            )
        )
    )

    fun selectRoute(route: RouteDefinition?) {
        _selectedRoute.value = route
        _isEditing.value = false
    }

    fun selectRouteFromCustom(customRoute: CustomRoute) {
        val steps: List<RouteStep> = try {
            val type = object : TypeToken<List<RouteStep>>() {}.type
            gson.fromJson(customRoute.stepsJson, type)
        } catch (e: Exception) {
            emptyList()
        }

        _selectedRoute.value = RouteDefinition(
            id = customRoute.id,
            title = customRoute.title,
            description = customRoute.description,
            steps = steps
        )
        _isEditing.value = false
    }

    fun startEditing() {
        _isEditing.value = true
    }

    fun cancelEditing() {
        _isEditing.value = false
    }

    fun saveRoute(title: String, description: String, steps: List<RouteStep>) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val id = _selectedRoute.value?.id ?: UUID.randomUUID().toString()

            val customRoute = CustomRoute(
                id = id,
                title = title,
                description = description,
                stepsJson = gson.toJson(steps),
                updatedAt = now
            )

            customRouteDao.insertOrUpdate(customRoute)
            _isEditing.value = false

            selectRouteFromCustom(customRoute)
        }
    }

    fun deleteRoute(route: CustomRoute) {
        viewModelScope.launch {
            customRouteDao.delete(route)
            if (_selectedRoute.value?.id == route.id) {
                _selectedRoute.value = null
            }
        }
    }

    fun createNewRoute() {
        _selectedRoute.value = RouteDefinition(
            id = "",
            title = "新規ルート",
            description = "",
            steps = listOf(
                RouteStep(1, "ステップ1", OperationKind.TEXT_GEN)
            )
        )
        _isEditing.value = true
    }
}
