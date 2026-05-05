package com.example.mad_a1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pokemon(val name: String, val region: String, val categories: List<String> = emptyList(),
    val imageResId: Int,
    val isFavorite: Boolean = false,
    val pokedexDesc: String
) : Parcelable
