package com.example.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TasksFragment : Fragment() {
    private lateinit var taskInputEditText: EditText
    private lateinit var addTaskButton: Button
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var taskManager: TaskManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskManager = TaskManager(requireContext())
        taskInputEditText = view.findViewById(R.id.taskInputEditText)
        addTaskButton = view.findViewById(R.id.addTaskButton)
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView)

        setupRecyclerView()
        setupAddTaskButton()
        loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks,
            onTaskCompleted = { task ->
                task.isCompleted = true
                task.completedTime = System.currentTimeMillis()
                Toast.makeText(context, "任務完成：${task.content}", Toast.LENGTH_SHORT).show()
                taskAdapter.removeTask(task)
                (activity as? MainActivity)?.onTaskCompleted(task)
                saveTasks()
            },
            onTaskAbandoned = { task ->
                task.isCompleted = false
                task.abandonedTime = System.currentTimeMillis()
                Toast.makeText(context, "放棄任務：${task.content}", Toast.LENGTH_SHORT).show()
                taskAdapter.removeTask(task)
                (activity as? MainActivity)?.onTaskAbandoned(task)
                saveTasks()
            }
        )

        tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }

        taskAdapter.setupSwipeToDelete(tasksRecyclerView)
    }

    private fun setupAddTaskButton() {
        addTaskButton.setOnClickListener {
            val taskContent = taskInputEditText.text.toString().trim()
            if (taskContent.isNotEmpty()) {
                val task = Task(content = taskContent)
                taskAdapter.addTask(task)
                taskInputEditText.text.clear()
                saveTasks()
            } else {
                Toast.makeText(context, "請輸入任務內容", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTasks() {
        taskManager.saveTasks(tasks, "current_tasks")
    }

    private fun loadTasks() {
        val savedTasks = taskManager.loadTasks("current_tasks")
        tasks.clear()
        tasks.addAll(savedTasks)
        taskAdapter.notifyDataSetChanged()
    }
} 