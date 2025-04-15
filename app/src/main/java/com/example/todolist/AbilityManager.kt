package com.example.todolist

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AbilityManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private var abilities = mutableMapOf<String, Ability>()
    private val _abilitiesLiveData = MutableLiveData<Map<String, Ability>>()
    val abilitiesLiveData: LiveData<Map<String, Ability>> = _abilitiesLiveData
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val PREFS_NAME = "ability_prefs"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_ABILITIES = "abilities"
        private const val DEFAULT_API_KEY = "AIzaSyBSutzPpAcxshCek2mz5iQS-k27djdQENY"
    }

    init {
        loadAbilities()
        // 如果沒有設置 API Key，則使用預設值
        if (getApiKey().isNullOrEmpty()) {
            saveApiKey(DEFAULT_API_KEY)
        }
    }

    fun reloadAbilities() {
        loadAbilities()
    }

    private fun loadAbilities() {
        try {
            val json = sharedPreferences.getString(KEY_ABILITIES, null)
            if (json != null) {
                val type = object : TypeToken<Map<String, Ability>>() {}.type
                abilities = gson.fromJson<Map<String, Ability>>(json, type).toMutableMap()
            } else {
                // 初始化所有能力
                Ability.ABILITY_TYPES.forEach { abilityName ->
                    abilities[abilityName] = Ability(abilityName, 0.0)
                }
                saveAbilities(abilities)
            }
            updateLiveData()
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果加載失敗，初始化所有能力
            Ability.ABILITY_TYPES.forEach { abilityName ->
                abilities[abilityName] = Ability(abilityName, 0.0)
            }
            saveAbilities(abilities)
            updateLiveData()
        }
    }

    private fun updateLiveData() {
        mainHandler.post {
            _abilitiesLiveData.value = abilities.toMap()
        }
    }

    private fun saveAbilities(abilities: Map<String, Ability>) {
        try {
            val json = gson.toJson(abilities)
            sharedPreferences.edit().putString(KEY_ABILITIES, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAbility(name: String): Ability {
        return abilities[name] ?: Ability(name, 0.0).also { abilities[name] = it }
    }

    fun getAllAbilities(): Map<String, Ability> {
        return abilities
    }

    fun updateAbility(abilityName: String, score: Double) {
        // 先更新內存中的數據
        val ability = abilities[abilityName] ?: Ability(abilityName, 0.0)
        ability.addExperience(score)
        abilities[abilityName] = ability
        
        // 在主線程更新 LiveData
        updateLiveData()
        
        // 在後台執行儲存操作
        Thread {
            saveAbilities(abilities)
        }.start()
    }

    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_API_KEY, DEFAULT_API_KEY)
    }
} 