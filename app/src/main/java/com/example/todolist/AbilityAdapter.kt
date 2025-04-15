package com.example.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AbilityAdapter(private var abilities: List<Ability>) :
    RecyclerView.Adapter<AbilityAdapter.AbilityViewHolder>() {

    class AbilityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.abilityNameTextView)
        val levelTextView: TextView = view.findViewById(R.id.abilityLevelTextView)
        val progressBar: ProgressBar = view.findViewById(R.id.abilityProgressBar)
        val experienceTextView: TextView = view.findViewById(R.id.abilityExperienceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbilityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ability, parent, false)
        return AbilityViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbilityViewHolder, position: Int) {
        val ability = abilities[position]
        holder.nameTextView.text = ability.name
        holder.levelTextView.text = "Lv.${ability.level}"
        holder.progressBar.max = Ability.EXPERIENCE_PER_LEVEL.toInt()
        holder.progressBar.progress = (ability.experience % Ability.EXPERIENCE_PER_LEVEL).toInt()
        holder.experienceTextView.text = "${ability.experience.toInt()}/${Ability.EXPERIENCE_PER_LEVEL.toInt()}"
    }

    override fun getItemCount() = abilities.size

    fun updateAbilities(newAbilities: List<Ability>) {
        abilities = newAbilities
        notifyDataSetChanged()
    }
} 