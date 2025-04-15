package com.example.todolist

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("tasks", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTasks(tasks: List<Task>, key: String) {
        val json = gson.toJson(tasks)
        sharedPreferences.edit().putString(key, json).apply()
    }

    fun loadTasks(key: String): List<Task> {
        val json = sharedPreferences.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveStats(totalTasks: Int, completedTasks: Int, streak: Int) {
        sharedPreferences.edit()
            .putInt("total_tasks", totalTasks)
            .putInt("completed_tasks", completedTasks)
            .putInt("streak", streak)
            .apply()
    }

    fun loadStats(): Triple<Int, Int, Int> {
        val totalTasks = sharedPreferences.getInt("total_tasks", 0)
        val completedTasks = sharedPreferences.getInt("completed_tasks", 0)
        val streak = sharedPreferences.getInt("streak", 0)
        return Triple(totalTasks, completedTasks, streak)
    }
} 