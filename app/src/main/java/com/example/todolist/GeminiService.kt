package com.example.todolist

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiService(private val apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash-lite",
        apiKey = apiKey
    )

    companion object {
        private const val TAG = "GeminiService"
    }

    suspend fun generateWelcomeMessage(): String = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                請用中文寫一段簡短的歡迎訊息，介紹這個待辦事項應用程式。
                重點說明：
                1. 這是一個可以追蹤任務完成情況的應用程式
                2. 完成任務後會自動分析並提升相關能力值
                3. 能力值包括：創意力、耐力、學習力、理解力、社交力、專注力、金錢力、語言力、溝通力和未知力
                4. 每個能力值都有等級和經驗值系統
                
                請用友善的語氣，不要超過 100 字。
            """.trimIndent()

            val response = model.generateContent(content { text(prompt) })
            return@withContext response.text ?: "歡迎使用待辦事項應用程式！完成任務後，系統會自動分析並提升相關能力值。"
        } catch (e: Exception) {
            Log.e(TAG, "生成歡迎訊息失敗", e)
            return@withContext "歡迎使用待辦事項應用程式！完成任務後，系統會自動分析並提升相關能力值。"
        }
    }

    suspend fun analyzeTask(task: Task): Map<String, Double> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                你是一個任務分析助手。請分析以下任務，並為每個能力值評分（0.0-5.0分）。
                
                任務內容：${task.content}
                任務狀態：${if (task.isCompleted) "已完成" else "已放棄"}
                
                請嚴格按照以下 JSON 格式返回結果：
                {
                    "創意力": 分數,
                    "耐力": 分數,
                    "學習力": 分數,
                    "理解力": 分數,
                    "社交力": 分數,
                    "專注力": 分數,
                    "金錢力": 分數,
                    "語言力": 分數,
                    "溝通力": 分數,
                    "未知力": 分數
                }
                
                評分標準：
                0.0分：完全不相關
                1.0分：稍微相關
                2.0分：有一定相關
                3.0分：明顯相關
                4.0分：高度相關
                5.0分：完全相關
                
                你可以根據任務內容的相關程度，給出 0.0 到 5.0 之間的小數分數。
                例如：如果任務與某個能力的相關程度介於"稍微相關"和"有一定相關"之間，可以給出 1.5 分。
                
                重要：請確保返回的是有效的 JSON 格式，且所有分數都是小數。不要添加任何額外的文字或說明。
            """.trimIndent()

            Log.d(TAG, "發送分析請求：${task.content}")
            val response = model.generateContent(content { text(prompt) })
            val result = parseResponse(response)
            Log.d(TAG, "收到分析結果：$result")
            return@withContext result
        } catch (e: Exception) {
            Log.e(TAG, "分析任務失敗", e)
            return@withContext emptyMap()
        }
    }

    private fun parseResponse(response: GenerateContentResponse): Map<String, Double> {
        return try {
            val jsonString = response.text ?: run {
                Log.e(TAG, "API 回應為空")
                return emptyMap()
            }
            Log.d(TAG, "API 回應：$jsonString")

            // 檢查回應是否包含 JSON 格式
            if (!jsonString.contains("{") || !jsonString.contains("}")) {
                Log.e(TAG, "API 回應不是有效的 JSON 格式")
                return emptyMap()
            }

            // 嘗試提取 JSON 部分
            val jsonStart = jsonString.indexOf("{")
            val jsonEnd = jsonString.lastIndexOf("}") + 1
            val jsonContent = jsonString.substring(jsonStart, jsonEnd)
            Log.d(TAG, "提取的 JSON 內容：$jsonContent")

            val jsonObject = JSONObject(jsonContent)
            val result = mutableMapOf<String, Double>()

            // 先解析除未知力外的所有能力值
            val otherAbilities = Ability.ABILITY_TYPES.filter { it != "未知力" }
            var hasHighScore = false

            otherAbilities.forEach { abilityName ->
                val score = jsonObject.optDouble(abilityName, 0.0)
                result[abilityName] = score
                Log.d(TAG, "解析能力值：$abilityName = $score")
                
                // 檢查是否有任何能力值大於2分
                if (score > 2.0) {
                    hasHighScore = true
                }
            }

            // 如果沒有任何能力值大於2分，則給未知力加1分
            if (!hasHighScore) {
                result["未知力"] = 1.0
                Log.d(TAG, "沒有明顯相關的能力值，給未知力加1分")
            } else {
                // 否則使用API返回的未知力分數
                result["未知力"] = jsonObject.optDouble("未知力", 0.0)
                Log.d(TAG, "解析能力值：未知力 = ${result["未知力"]}")
            }

            if (result.isEmpty()) {
                Log.e(TAG, "沒有找到任何能力值分數")
                return emptyMap()
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "解析回應失敗", e)
            emptyMap()
        }
    }
} 