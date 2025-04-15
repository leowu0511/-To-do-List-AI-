package com.example.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {
    private lateinit var completedTasksRecyclerView: RecyclerView
    private lateinit var abandonedTasksRecyclerView: RecyclerView
    private lateinit var completedTasksAdapter: TaskAdapter
    private lateinit var abandonedTasksAdapter: TaskAdapter
    private val completedTasks = mutableListOf<Task>()
    private val abandonedTasks = mutableListOf<Task>()
    private lateinit var taskManager: TaskManager
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskManager = TaskManager(requireContext())
        completedTasksRecyclerView = view.findViewById(R.id.completedTasksRecyclerView)
        abandonedTasksRecyclerView = view.findViewById(R.id.abandonedTasksRecyclerView)

        setupRecyclerViews()
        loadTasks()
    }

    private fun setupRecyclerViews() {
        completedTasksAdapter = TaskAdapter(
            completedTasks,
            onTaskCompleted = { },
            onTaskAbandoned = { }
        )

        abandonedTasksAdapter = TaskAdapter(
            abandonedTasks,
            onTaskCompleted = { },
            onTaskAbandoned = { }
        )

        completedTasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = completedTasksAdapter
        }

        abandonedTasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = abandonedTasksAdapter
        }
    }

    fun addCompletedTask(task: Task) {
        completedTasks.add(0, task)
        completedTasksAdapter.notifyItemInserted(0)
        saveTasks()
    }

    fun addAbandonedTask(task: Task) {
        abandonedTasks.add(0, task)
        abandonedTasksAdapter.notifyItemInserted(0)
        saveTasks()
    }

    private fun saveTasks() {
        taskManager.saveTasks(completedTasks, "completed_tasks")
        taskManager.saveTasks(abandonedTasks, "abandoned_tasks")
    }

    private fun loadTasks() {
        try {
            val savedCompletedTasks = taskManager.loadTasks("completed_tasks")
            val savedAbandonedTasks = taskManager.loadTasks("abandoned_tasks")
            
            completedTasks.clear()
            abandonedTasks.clear()
            
            completedTasks.addAll(savedCompletedTasks.sortedByDescending { it.completedTime })
            abandonedTasks.addAll(savedAbandonedTasks.sortedByDescending { it.abandonedTime })
            
            completedTasksAdapter.notifyDataSetChanged()
            abandonedTasksAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 