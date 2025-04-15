package com.example.todolist

import java.io.Serializable

data class Ability(
    val name: String,
    var experience: Double = 0.0,
    var level: Int = 1
) : Serializable {
    companion object {
        const val EXPERIENCE_PER_LEVEL = 50.0
        val ABILITY_TYPES = listOf(
            "創意力", "耐力", "學習力", "理解力", "社交力",
            "專注力", "金錢力", "語言力", "溝通力", "未知力"
        )
    }

    fun addExperience(exp: Double) {
        experience += exp
        while (experience >= EXPERIENCE_PER_LEVEL) {
            experience -= EXPERIENCE_PER_LEVEL
            level++
        }
    }
} 