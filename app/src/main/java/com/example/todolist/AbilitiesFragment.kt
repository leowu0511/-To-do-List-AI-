package com.example.todolist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class AbilitiesFragment : Fragment() {
    private lateinit var apiKeyEditText: EditText
    private lateinit var saveApiKeyButton: Button
    private lateinit var abilitiesRecyclerView: RecyclerView
    private lateinit var abilityManager: AbilityManager
    private lateinit var abilityAdapter: AbilityAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    companion object {
        private const val TAG = "AbilitiesFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_abilities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        abilityManager = AbilityManager(requireContext())
        apiKeyEditText = view.findViewById(R.id.apiKeyEditText)
        saveApiKeyButton = view.findViewById(R.id.saveApiKeyButton)
        abilitiesRecyclerView = view.findViewById(R.id.abilitiesRecyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        setupApiKeySection()
        setupAbilitiesList()
        setupSwipeRefresh()
        observeAbilities()
    }

    private fun observeAbilities() {
        abilityManager.abilitiesLiveData.observe(viewLifecycleOwner) { abilities ->
            Log.d(TAG, "收到能力值更新：$abilities")
            refreshAbilitiesList(abilities)
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            // 重新載入能力值
            abilityManager.reloadAbilities()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupApiKeySection() {
        // 載入已保存的 API Key
        abilityManager.getApiKey()?.let { apiKey ->
            apiKeyEditText.setText(apiKey)
        }

        saveApiKeyButton.setOnClickListener {
            val apiKey = apiKeyEditText.text.toString().trim()
            if (apiKey.isNotEmpty()) {
                abilityManager.saveApiKey(apiKey)
                Toast.makeText(context, "Gemini API Key 已保存", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "請輸入 Gemini API Key", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAbilitiesList() {
        // 初始化 RecyclerView
        abilityAdapter = AbilityAdapter(emptyList())
        abilitiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = abilityAdapter
        }
    }

    private fun refreshAbilitiesList(abilities: Map<String, Ability>) {
        Log.d(TAG, "刷新能力值列表：$abilities")
        abilityAdapter.updateAbilities(abilities.values.toList())
    }

    fun updateAbilities() {
        // 重新載入能力值
        abilityManager.reloadAbilities()
    }
} 