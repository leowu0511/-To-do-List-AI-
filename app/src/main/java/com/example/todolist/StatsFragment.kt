package com.example.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class StatsFragment : Fragment() {
    private lateinit var totalTasksValue: TextView
    private lateinit var completedTasksValue: TextView
    private lateinit var streakValue: TextView
    private lateinit var taskManager: TaskManager

    private var totalTasks = 0
    private var completedTasks = 0
    private var streak = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskManager = TaskManager(requireContext())
        totalTasksValue = view.findViewById(R.id.totalTasksValue)
        completedTasksValue = view.findViewById(R.id.completedTasksValue)
        streakValue = view.findViewById(R.id.streakValue)

        loadStats()
        updateStats()
    }

    private fun updateStats() {
        totalTasksValue.text = totalTasks.toString()
        val completionRate = if (totalTasks > 0) {
            (completedTasks.toFloat() / totalTasks * 100).toInt()
        } else {
            0
        }
        completedTasksValue.text = "$completionRate%"
        streakValue.text = streak.toString()
        saveStats()
    }

    private fun saveStats() {
        taskManager.saveStats(totalTasks, completedTasks, streak)
    }

    private fun loadStats() {
        val (loadedTotalTasks, loadedCompletedTasks, loadedStreak) = taskManager.loadStats()
        totalTasks = loadedTotalTasks
        completedTasks = loadedCompletedTasks
        streak = loadedStreak
    }

    fun onTaskCompleted() {
        completedTasks++
        totalTasks++
        streak++
        updateStats()
    }

    fun onTaskAbandoned() {
        totalTasks++
        streak = 0
        updateStats()
    }
} 