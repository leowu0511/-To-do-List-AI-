package com.example.todolist

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskCompleted: (Task) -> Unit,
    private val onTaskAbandoned: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTextView: TextView = view.findViewById(R.id.taskTextView)
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskTextView.text = task.content
        
        val timeText = when {
            task.completedTime > 0 -> "完成於：${dateFormat.format(Date(task.completedTime))}"
            task.abandonedTime > 0 -> "放棄於：${dateFormat.format(Date(task.abandonedTime))}"
            else -> ""
        }
        holder.timeTextView.text = timeText
    }

    override fun getItemCount() = tasks.size

    fun addTask(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    fun removeTask(task: Task) {
        val position = tasks.indexOfFirst { it.id == task.id }
        if (position != -1) {
            tasks.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            private val completeIcon: Drawable? = ContextCompat.getDrawable(
                recyclerView.context,
                android.R.drawable.ic_menu_edit
            )
            private val deleteIcon: Drawable? = ContextCompat.getDrawable(
                recyclerView.context,
                android.R.drawable.ic_menu_close_clear_cancel
            )
            private val completeBackground = ColorDrawable(Color.GREEN)
            private val deleteBackground = ColorDrawable(Color.RED)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = tasks[position]
                    when (direction) {
                        ItemTouchHelper.RIGHT -> {
                            onTaskCompleted(task)
                        }
                        ItemTouchHelper.LEFT -> {
                            onTaskAbandoned(task)
                        }
                    }
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - (completeIcon?.intrinsicHeight ?: 0)) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + (completeIcon?.intrinsicHeight ?: 0)

                if (dX > 0) { // 右滑
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = iconLeft + (completeIcon?.intrinsicWidth ?: 0)
                    completeIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    completeBackground.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                    completeBackground.draw(c)
                    completeIcon?.draw(c)
                } else if (dX < 0) { // 左滑
                    val iconRight = itemView.right - iconMargin
                    val iconLeft = iconRight - (deleteIcon?.intrinsicWidth ?: 0)
                    deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteBackground.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    deleteBackground.draw(c)
                    deleteIcon?.draw(c)
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }
} 