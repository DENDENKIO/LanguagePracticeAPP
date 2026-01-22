package com.example.languagepracticev3.data.model

data class RouteDefinition(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val steps: List<RouteStep> = emptyList()
)

data class RouteStep(
    val stepNumber: Int = 0,
    val title: String = "",
    val operation: OperationKind = OperationKind.TEXT_GEN,
    val inputBindings: Map<String, String> = emptyMap(),
    val fixedLength: LengthProfile? = null,
    val fixedTone: String? = null
)

// OperationKind は OperationKind.kt で定義済みのため削除

enum class LengthProfile {
    SHORT,
    MEDIUM,
    LONG,
    VERY_LONG
}

class LpExecutionContext {
    val stepOutputs: MutableMap<Int, Map<String, String>> = mutableMapOf()
    val stepWorkIds: MutableMap<Int, Long> = mutableMapOf()

    fun addOutput(stepNumber: Int, output: Map<String, String>, workId: Long? = null) {
        stepOutputs[stepNumber] = output
        workId?.let { stepWorkIds[stepNumber] = it }
    }

    fun getPreviousOutputValue(key: String): String {
        for (i in 100 downTo 1) {
            stepOutputs[i]?.get(key)?.let { return it }
        }
        return ""
    }

    fun getPreviousWorkId(): Long? {
        for (i in 100 downTo 1) {
            stepWorkIds[i]?.let { return it }
        }
        return null
    }
}
