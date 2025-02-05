package com.example.parsing.example.repository

import com.example.parsing.example.models.Cat

class CatRepository {
    private val cats = mutableListOf(
        Cat("Whiskers", 2, true),
        Cat("Mittens", 3, false)
    )

    fun getAllCats(): List<Cat> = cats

    fun getCatByName(name: String): Cat? = cats.find { it.name == name }

    fun addCat(cat: Cat) {
        cats.add(cat)
    }

    fun updateCat(name: String, updatedCat: Cat): Boolean {
        val index = cats.indexOfFirst { it.name == name }
        return if (index != -1) {
            cats[index] = updatedCat
            true
        } else {
            false
        }
    }

    fun partialUpdateCat(name: String, updates: Map<String, Any>): Boolean {
        val index = cats.indexOfFirst { it.name == name }
        if (index == -1) return false

        val existingCat = cats[index]

        // Criando uma nova instância com valores atualizados
        val updatedCat = existingCat.copy(
            name = updates["name"] as? String ?: existingCat.name,
            age = updates["age"] as? Int ?: existingCat.age,
            isMale = updates["isMale"] as? Boolean ?: existingCat.isMale
        )

        cats[index] = updatedCat
        return true
    }

    fun deleteCat(name: String): Boolean {
        return cats.removeIf { it.name == name }
    }

    fun countCats(): Int = cats.size
}
