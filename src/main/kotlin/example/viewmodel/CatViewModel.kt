package com.example.parsing.example.viewmodel


import com.example.parsing.example.models.Cat
import com.example.parsing.example.repository.CatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers


class CatViewModel(private val repository: CatRepository) {
    val scope = CoroutineScope(Dispatchers.Main)

    private val _cats = MutableStateFlow<List<Cat>>(emptyList())
    val cats: StateFlow<List<Cat>> get() = _cats

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun loadCats() {

        scope.launch {
            try {
                val allCats = repository.getAllCats()
                _cats.value = allCats
            } catch (e: Exception) {
                _error.value = "Failed to load cats"
            }
        }
    }


    fun addCat(cat: Cat) {
        scope.launch {
            try {
                repository.addCat(cat)
                loadCats()
            } catch (e: Exception) {
                _error.value = "Failed to add cat"
            }
        }
    }


    fun loadCatByName(name: String) {
        scope.launch {
            try {
                val cat = repository.getCatByName(name)
                if (cat != null) {
                    _cats.value = listOf(cat)
                } else {
                    _error.value = "Cat not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to find cat"
            }
        }
    }
}
