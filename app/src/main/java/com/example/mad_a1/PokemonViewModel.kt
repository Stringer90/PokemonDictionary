package com.example.mad_a1

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class PokemonViewModel : ViewModel() {

    private val _all = MutableLiveData(PokemonData.allPokemon)

    // Flip favorite state for the selected Pokemon
    fun toggleFavorite(pokemon: Pokemon) {
        val newList = _all.value.orEmpty().map {
            if (it == pokemon) {
                it.copy(isFavorite = !it.isFavorite)
            } else {
                it
            }
        }
        _all.value = newList
    }

    // LiveData for search query, selected categories, and favorites filter
    val query = MutableLiveData("")
    val selectedCategories = MutableLiveData<Set<String>>(emptySet())
    val favoritesOnly = MutableLiveData(false)

    // Collect all categories from current list
    val allCategories: LiveData<List<String>> = _all.map { list ->
        list.flatMap { it.categories }.toSet().toList().sorted()
    }

    // LiveData that recomputes whenever any filter changes
    val filtered: LiveData<List<Pokemon>> = MediatorLiveData<List<Pokemon>>().apply {
        fun recompute() {
            val allNow = _all.value.orEmpty()
            val q = query.value.orEmpty().trim().lowercase()
            val cats = selectedCategories.value.orEmpty()
            val favOnly = favoritesOnly.value ?: false

            // Apply filters: search, categories, and favorite-only
            val filteredList = allNow.filter { p ->
                val matchesQuery = q.isEmpty()
                        || p.name.contains(q, ignoreCase = true)
                        || p.pokedexDesc.contains(q, ignoreCase = true)
                val matchesCategory = cats.isEmpty() || p.categories.any { it in cats }
                val matchesFavorite = !favOnly || p.isFavorite

                matchesQuery && matchesCategory && matchesFavorite
            }

            // Sort favorites first, then alphabetically
            value = filteredList.sortedWith(
                compareByDescending<Pokemon> { it.isFavorite }
                    .thenBy { it.name.lowercase() }
            )
        }

        // Recompute when any source LiveData changes
        addSource(_all) { recompute() }
        addSource(query) { recompute() }
        addSource(selectedCategories) { recompute() }
        addSource(favoritesOnly) { recompute() }
    }

}
