package com.example.todolist

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tasksFragment: TasksFragment
    private lateinit var historyFragment: HistoryFragment
    private lateinit var abilitiesFragment: AbilitiesFragment
    private lateinit var abilityManager: AbilityManager

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        abilityManager = AbilityManager(this)
        viewPager = findViewById(R.id.viewPager)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupFragments()
        setupNavigation()
        showWelcomeMessage()
    }

    private fun showWelcomeMessage() {
        lifecycleScope.launch {
            try {
                val geminiService = GeminiService(abilityManager.getApiKey() ?: return@launch)
                val welcomeMessage = geminiService.generateWelcomeMessage()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("歡迎使用")
                    .setMessage(welcomeMessage)
                    .setPositiveButton("開始使用") { dialog, _ -> dialog.dismiss() }
                    .show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "無法載入歡迎訊息", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFragments() {
        tasksFragment = TasksFragment()
        historyFragment = HistoryFragment()
        abilitiesFragment = AbilitiesFragment()

        val fragments = listOf(tasksFragment, historyFragment, abilitiesFragment)
        val pagerAdapter = ViewPagerAdapter(this, fragments)
        viewPager.adapter = pagerAdapter
    }

    private fun setupNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_tasks -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.navigation_history -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.navigation_stats -> {
                    viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigation.menu.getItem(position).isChecked = true
            }
        })
    }

    fun onTaskCompleted(task: Task) {
        Log.d(TAG, "任務完成：${task.content}")
        historyFragment.addCompletedTask(task)
        analyzeTaskWithGemini(task)
    }

    fun onTaskAbandoned(task: Task) {
        Log.d(TAG, "放棄任務：${task.content}")
        historyFragment.addAbandonedTask(task)
        analyzeTaskWithGemini(task)
    }

    private fun analyzeTaskWithGemini(task: Task) {
        val apiKey = abilityManager.getApiKey()
        if (apiKey.isNullOrEmpty()) {
            Log.e(TAG, "API Key 為空")
            Toast.makeText(this, "請先在能力值頁面設置 Gemini API Key", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d(TAG, "開始分析任務：${task.content}")
                val geminiService = GeminiService(apiKey)
                val scores = geminiService.analyzeTask(task)
                
                if (scores.isEmpty()) {
                    Log.e(TAG, "API 返回空的分數")
                    Toast.makeText(this@MainActivity, "無法分析任務", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                Log.d(TAG, "獲得的分數：$scores")
                scores.forEach { (abilityName, score) ->
                    Log.d(TAG, "更新能力值：$abilityName, 分數：$score")
                    abilityManager.updateAbility(abilityName, score)
                }
                
                notifyAbilitiesUpdated()
                Toast.makeText(this@MainActivity, "能力值已更新，可前往能力值頁面查看", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "分析任務失敗", e)
                Toast.makeText(this@MainActivity, "分析任務失敗：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun notifyAbilitiesUpdated() {
        if (viewPager.currentItem == 2) {
            abilitiesFragment.updateAbilities()
        }
    }
}

class ViewPagerAdapter(
    activity: AppCompatActivity,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}