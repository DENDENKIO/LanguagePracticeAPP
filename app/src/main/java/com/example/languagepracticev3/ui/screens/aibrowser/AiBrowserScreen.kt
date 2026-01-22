// app/src/main/java/com/example/languagepracticev3/ui/screens/aibrowser/AiBrowserScreen.kt
package com.example.languagepracticev3.ui.screens.aibrowser

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.languagepracticev3.data.model.AiSiteProfile
import com.example.languagepracticev3.data.model.LpConstants  // ← 修正: LpConstantsを直接インポート
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * AI WebView 自動送信画面
 * WPF版 BrowserWindow.xaml.cs をKotlin/Composeに移植
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiBrowserScreen(
    siteProfile: AiSiteProfile,
    prompt: String,
    onResultReceived: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var webView by remember { mutableStateOf<WebView?>(null) }
    var statusMessage by remember { mutableStateOf("読み込み中...") }
    var isLoading by remember { mutableStateOf(true) }
    var hasInjected by remember { mutableStateOf(false) }
    var isMonitoring by remember { mutableStateOf(false) }
    var lastTextLength by remember { mutableIntStateOf(0) }
    var stableCount by remember { mutableIntStateOf(0) }

    // 修正: PromptBuilder.LpConstants → LpConstants
    val doneSentinel = LpConstants.DONE_SENTINEL
    val promptSentinelCount = remember { countSentinel(prompt, doneSentinel) }

    // JavaScript Interface for receiving results
    val jsInterface = remember {
        object {
            @JavascriptInterface
            fun onTextReceived(text: String) {
                // メインスレッドで処理
            }
        }
    }

    // 監視ループ
    LaunchedEffect(isMonitoring) {
        if (!isMonitoring) return@LaunchedEffect

        while (isMonitoring) {
            delay(1000)
            webView?.let { wv ->
                wv.evaluateJavascript("document.body.innerText") { rawText ->
                    val currentText = rawText
                        .removeSurrounding("\"")
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")

                    val currentLength = currentText.length
                    val totalSentinelCount = countSentinel(currentText, doneSentinel)
                    val requiredCount = promptSentinelCount + 1

                    statusMessage = "監視中: Sentinel=$totalSentinelCount/$requiredCount, 長さ=$currentLength, 安定=$stableCount/5"

                    // センチネルが必要数に達していない場合は待機
                    if (totalSentinelCount < requiredCount) {
                        stableCount = 0
                        lastTextLength = currentLength
                        return@evaluateJavascript
                    }

                    // テキスト長が安定しているかチェック
                    if (currentLength != lastTextLength) {
                        stableCount = 0
                        lastTextLength = currentLength
                    } else {
                        stableCount++
                    }

                    // 安定したら結果を取得
                    if (stableCount >= 5) {
                        isMonitoring = false
                        statusMessage = "生成完了！結果を取得中..."

                        scope.launch {
                            delay(2000)
                            wv.evaluateJavascript("document.body.innerText") { finalRaw ->
                                val fullText = finalRaw
                                    .removeSurrounding("\"")
                                    .replace("\\n", "\n")
                                    .replace("\\\"", "\"")

                                val resultText = extractAiResponse(fullText, promptSentinelCount, doneSentinel)
                                onResultReceived(resultText)
                            }
                        }
                    }
                }
            }
        }
    }

    // プロンプト注入関数
    fun injectPrompt() {
        webView?.let { wv ->
            statusMessage = "自動入力実行中..."
            val safePrompt = jsEscape(prompt)
            val script = buildInjectScript(siteProfile.id, safePrompt)

            wv.evaluateJavascript(script) { result ->
                if (result.contains("INPUT_NOT_FOUND")) {
                    statusMessage = "入力欄が見つかりません。手動で入力してください。"
                } else {
                    statusMessage = "入力完了。生成完了を待機中..."
                    hasInjected = true
                    lastTextLength = 0
                    stableCount = 0
                    isMonitoring = true
                }
            }
        }
    }

    // 戻るボタン処理
    BackHandler {
        onDismiss()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(siteProfile.name) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "閉じる")
                    }
                },
                actions = {
                    IconButton(onClick = { injectPrompt() }) {
                        Icon(Icons.Default.Send, "再送信")
                    }
                    IconButton(onClick = {
                        // クリップボードから貼り付け
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                        if (clipText.isNotBlank()) {
                            onResultReceived(clipText)
                        }
                    }) {
                        Icon(Icons.Default.ContentPaste, "貼り付けて閉じる")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading || isMonitoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    @SuppressLint("SetJavaScriptEnabled")
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.userAgentString = settings.userAgentString.replace("; wv", "")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false

                            if (!hasInjected) {
                                statusMessage = "読み込み完了。3秒後に自動入力..."
                                scope.launch {
                                    delay(3000)
                                    injectPrompt()
                                }
                            }
                        }
                    }

                    webChromeClient = WebChromeClient()

                    webView = this
                    loadUrl(siteProfile.url)
                }
            },
            update = { /* WebViewは初期化時のみ設定 */ }
        )
    }
}

// ==========================================
// ヘルパー関数
// ==========================================

private fun countSentinel(text: String, sentinel: String): Int {
    if (text.isEmpty() || sentinel.isEmpty()) return 0
    var count = 0
    var pos = 0
    while (true) {
        pos = text.indexOf(sentinel, pos)
        if (pos == -1) break
        count++
        pos += sentinel.length
    }
    return count
}

private fun jsEscape(s: String): String {
    return s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "")
}

private fun extractAiResponse(fullText: String, promptSentinelCount: Int, sentinel: String): String {
    // プロンプト内のセンチネルをスキップ
    var pos = 0
    for (i in 0 until promptSentinelCount) {
        pos = fullText.indexOf(sentinel, pos)
        if (pos == -1) return fullText
        pos += sentinel.length
    }

    val afterPrompt = if (pos > 0) fullText.substring(pos) else fullText

    // AI出力内のセンチネルを探す
    val aiSentinelPos = afterPrompt.indexOf(sentinel)
    if (aiSentinelPos == -1) return afterPrompt

    // 修正: LpConstants.MarkerBegin.values を正しくイテレート
    val markerValues: Collection<String> = LpConstants.MarkerBegin.values
    var bestMarkerPos = -1

    for (marker in markerValues) {
        val markerPos = afterPrompt.indexOf(marker)
        if (markerPos != -1 && markerPos < aiSentinelPos && markerPos > bestMarkerPos) {
            bestMarkerPos = markerPos
        }
    }

    return if (bestMarkerPos != -1) {
        afterPrompt.substring(bestMarkerPos, aiSentinelPos + sentinel.length)
    } else {
        val extractStart = maxOf(0, aiSentinelPos - 10000)
        afterPrompt.substring(extractStart, aiSentinelPos + sentinel.length)
    }
}

private fun buildInjectScript(siteId: String, safePrompt: String): String {
    // Perplexity用
    if (siteId == "PERPLEXITY") {
        return """
(function() {
  var prompt = "$safePrompt";
  
  var input = 
    document.querySelector('#ask-input[contenteditable="true"][data-lexical-editor="true"]') ||
    document.querySelector('#ask-input[contenteditable="true"]') ||
    document.querySelector('div[contenteditable="true"][role="textbox"]') ||
    document.querySelector('textarea');
  
  if (!input) return 'INPUT_NOT_FOUND';
  
  input.focus();
  
  try {
    var sel = window.getSelection();
    var range = document.createRange();
    range.selectNodeContents(input);
    sel.removeAllRanges();
    sel.addRange(range);
    document.execCommand('delete');
  } catch(e) {}
  
  var ok = false;
  try {
    ok = document.execCommand('insertText', false, prompt);
  } catch(e) {
    ok = false;
  }
  
  if (!ok) {
    try {
      input.textContent = prompt;
      input.dispatchEvent(new InputEvent('input', { bubbles: true, data: prompt, inputType: 'insertText' }));
    } catch(e) {}
  }
  
  setTimeout(() => {
    var sendBtn = 
      document.querySelector('button[aria-label="送信"]') ||
      document.querySelector('button[aria-label*="Send"]') ||
      document.querySelector('button[type="submit"]');
    
    if (sendBtn) sendBtn.click();
    else {
      input.dispatchEvent(new KeyboardEvent('keydown', { bubbles: true, key: 'Enter', code: 'Enter', keyCode: 13 }));
    }
  }, 500);
  
  return 'INPUT_SET';
})();
        """.trimIndent()
    }

    // 汎用（Genspark等）
    return """
(function() {
  function pickInput() {
    return document.querySelector('textarea') ||
      document.querySelector('div[contenteditable="true"]') ||
      document.querySelector('input[type="text"]');
  }
  
  var input = pickInput();
  if (!input) return 'INPUT_NOT_FOUND';
  
  input.focus();
  
  if (input.tagName === 'TEXTAREA' || input.tagName === 'INPUT') {
    input.value = "$safePrompt";
  } else {
    input.innerText = "$safePrompt";
  }
  
  input.dispatchEvent(new Event('input', { bubbles: true }));
  
  setTimeout(() => {
    var sendBtn = 
      document.querySelector('button[aria-label="送信"]') ||
      document.querySelector('button[type="submit"]') ||
      document.querySelector('button[aria-label*="Send"]') ||
      document.querySelector('button[aria-label*="Submit"]');
    
    if (!sendBtn) {
      var buttons = Array.from(document.querySelectorAll('button'));
      sendBtn = buttons.find(b =>
        (b.innerText && (b.innerText.toLowerCase().includes('send') || b.innerText.toLowerCase().includes('submit') || b.innerText.includes('送信'))) ||
        (b.getAttribute('aria-label') && (b.getAttribute('aria-label').toLowerCase().includes('send') || b.getAttribute('aria-label').toLowerCase().includes('submit')))
      );
    }
    
    if (sendBtn) sendBtn.click();
    else {
      input.dispatchEvent(new KeyboardEvent('keydown', { bubbles: true, key: 'Enter', code: 'Enter', keyCode: 13 }));
    }
  }, 600);
  
  return 'INPUT_SET';
})();
    """.trimIndent()
}
