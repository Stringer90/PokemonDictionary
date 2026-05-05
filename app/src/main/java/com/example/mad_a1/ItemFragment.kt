package com.example.mad_a1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.widget.Button
import android.widget.ImageButton

class ItemFragment : Fragment() {

    private val vm: PokemonViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = this.arguments
        val returnBtn = view.findViewById<Button>(R.id.returnBtn)
        val namePokemon = view.findViewById<TextView>(R.id.namePokemon)
        val regionPokemon = view.findViewById<TextView>(R.id.regionPokemon)
        val typePokemon = view.findViewById<TextView>(R.id.typePokemon)
        val pokedexDesc = view.findViewById<TextView>(R.id.pokedexDesc)
        val imageView = view.findViewById<ImageView>(R.id.pokemonImage)

        val favBtn = view.findViewById<ImageButton>(R.id.buttonFavoriteItem)

        if (bundle?.isEmpty == false) {
            val item = bundle.getParcelable<Pokemon>("item_key")
            item?.let { p ->
                // Populate UI with Pokemon details
                namePokemon.text = p.name
                regionPokemon.text = "Region: ${p.region}"
                typePokemon.text = "Type: ${p.categories.joinToString(", ")}"
                pokedexDesc.text = p.pokedexDesc
                imageView.setImageResource(p.imageResId)

                // Set initial favorite icon based on state
                favBtn.setImageResource(if (p.isFavorite) R.drawable.filled_star else R.drawable.outline_star)

                // Toggle favorite when pressed, update both ViewModel + UI
                var isFav = p.isFavorite
                favBtn.setOnClickListener {
                    vm.toggleFavorite(p)
                    isFav = !isFav
                    favBtn.setImageResource(if (isFav) R.drawable.filled_star else R.drawable.outline_star)
                }
            }
        }

        // Return to previous fragment when back button pressed
        returnBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    // Create fragment, passing in Pokemon data
    companion object {
        fun newObject(pokemon: Pokemon): ItemFragment{
            val itemFragment = ItemFragment()
            val bundle = Bundle()
            bundle.putParcelable("item_key",pokemon)
            itemFragment.setArguments(bundle)
            return itemFragment
        }
    }
}


