package com.example.mad_a1

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListFragment : Fragment(R.layout.fragment_list) {

    private val vm: PokemonViewModel by activityViewModels()
    private lateinit var rv: RecyclerView
    private lateinit var lm: LinearLayoutManager
    private lateinit var adapter: PokemonAdapter

    private var pendingRvState: Parcelable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.recyclerView)
        val search = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.searchEditText)
        val categoryDropdown = view.findViewById<AutoCompleteTextView>(R.id.categoryDropdown)
        val favoritesOnly = view.findViewById<android.widget.ImageButton>(R.id.favoritesOnly)

        // Handle RecyclerView span count for different screen sizes and orientations
        val config = resources.configuration
        val orientation = config.orientation
        val screenSize = config.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val metrics = resources.displayMetrics
        val widthDp = metrics.widthPixels / metrics.density

        val spanCount = when {
            screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL -> {
                1
            }

            screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && widthDp < 720 -> {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) 1 else 2
            }

            screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL -> {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) 1 else 3
            }

            else -> {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
            }
        }

        lm = GridLayoutManager(requireActivity(), spanCount)
        rv.layoutManager = lm

        // Adapter with callbacks for more details, item click, and favorite toggle
        adapter = PokemonAdapter(
            onMoreClicked = { pokemon ->
                val itemFragment = ItemFragment.newObject(pokemon)

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.list_container, itemFragment, "ItemFragment")
                    .addToBackStack(null)
                    .commit()
            },
            onItemClicked = { pokemon ->
                Toast.makeText(
                    requireActivity(),
                    "Selected ${pokemon.name}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onFavoriteClicked = { pokemon ->
                vm.toggleFavorite(pokemon)
            }
        ).apply {
            // Prevent restoring state when adapter is empty
            stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        rv.adapter = adapter

        // Restore scroll state if saved
        pendingRvState = savedInstanceState?.let { state ->
            if (Build.VERSION.SDK_INT >= 33) {
                state.getParcelable("rv_state", Parcelable::class.java)
            } else {
                @Suppress("DEPRECATION")
                state.getParcelable<Parcelable>("rv_state")
            }
        }

        // Update query filter when typing
        search.doOnTextChanged { text, _, _, _ ->
            vm.query.value = text?.toString().orEmpty()
        }

        // Populate dropdown categories + handle selection
        vm.allCategories.observe(viewLifecycleOwner) { cats ->
            val items = listOf("All") + cats
            val dropAdapter = ArrayAdapter(requireActivity(),
                android.R.layout.simple_list_item_1, items)
            categoryDropdown.setAdapter(dropAdapter)
        }
        categoryDropdown.setOnItemClickListener { parent, _, position, _ ->
            val chosen = parent.getItemAtPosition(position) as String
            if (chosen == "All") {
                vm.selectedCategories.value = emptySet()
            } else {
                vm.selectedCategories.value = setOf(chosen)
            }
        }
        categoryDropdown.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrBlank()) vm.selectedCategories.value = emptySet()
        }

        // Toggle favorites-only mode
        favoritesOnly.setOnClickListener {
            val currentState = vm.favoritesOnly.value ?: false
            vm.favoritesOnly.value = !currentState
        }
        vm.favoritesOnly.observe(viewLifecycleOwner) { isFilterOn ->
            if (isFilterOn) {
                favoritesOnly.setImageResource(R.drawable.filled_star)
            } else {
                favoritesOnly.setImageResource(R.drawable.outline_star)
            }
        }

        // Submit filtered list to adapter
        // Restore scroll if needed
        vm.filtered.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list) {
                pendingRvState?.let { s ->
                    rv.layoutManager?.onRestoreInstanceState(s)
                    pendingRvState = null
                    return@submitList
                }
            }
        }
    }

    // Save scroll position
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this::rv.isInitialized){
            rv.layoutManager?.onSaveInstanceState()?.let { ss ->
                outState.putParcelable("rv_state", ss)
            }
        }
    }
}
