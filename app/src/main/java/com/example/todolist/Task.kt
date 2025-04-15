package com.example.todolist

import java.io.Serializable

data class Task(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    var isCompleted: Boolean = false,
    var completedTime: Long = 0,
    var abandonedTime: Long = 0
) : Serializable 