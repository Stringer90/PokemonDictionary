package com.example.mad_a1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PokemonAdapter(
    private val onMoreClicked: (Pokemon) -> Unit,
    private val onItemClicked: (Pokemon) -> Unit,
    private val onFavoriteClicked: (Pokemon) -> Unit
) : ListAdapter<Pokemon, PokemonAdapter.PokemonViewHolder>(Diff) {

    // Checking item identity/content
    object Diff : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(a: Pokemon, b: Pokemon): Boolean {
            return a.name == b.name
        }

        override fun areContentsTheSame(a: Pokemon, b: Pokemon): Boolean {
            return a == b
        }
    }

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.textName)
        val textRegion: TextView = view.findViewById(R.id.textRegion)
        val textDesc: TextView = view.findViewById(R.id.textDesc)
        val btnMore: Button = view.findViewById(R.id.btnMore)
        val image: ImageView = view.findViewById(R.id.pokemonImage)

        val buttonFavorite: ImageButton = view.findViewById(R.id.buttonFavorite)
        val chips: com.google.android.material.chip.ChipGroup =
            view.findViewById(R.id.chipsCategories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        // Fill UI with Pokemon data
        val pokemon = getItem(position)
        holder.image.setImageResource(pokemon.imageResId)
        holder.textName.text = pokemon.name
        holder.textRegion.text = pokemon.region
        holder.textDesc.text = pokemon.pokedexDesc

        // Render categories as chips
        holder.chips.removeAllViews()
        if (pokemon.categories.isEmpty()) {
            holder.chips.visibility = View.GONE
        } else {
            holder.chips.visibility = View.VISIBLE
            pokemon.categories.forEach { label ->
                val chip = com.google.android.material.chip.Chip(holder.itemView.context).apply {
                    text = label
                    isClickable = false
                    isCheckable = false
                    setEnsureMinTouchTargetSize(false)
                    textSize = 12f
                }
                holder.chips.addView(chip)
            }
        }

        // Update favorite star
        if (pokemon.isFavorite) {
            holder.buttonFavorite.setImageResource(R.drawable.filled_star)
        } else {
            holder.buttonFavorite.setImageResource(R.drawable.outline_star)
        }

        // Click listeners for favorite button, "More Details" button, and list item
        holder.buttonFavorite.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onFavoriteClicked(getItem(pos))
        }
        holder.btnMore.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onMoreClicked(getItem(pos))
        }
        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onItemClicked(getItem(pos))
        }
    }
}

