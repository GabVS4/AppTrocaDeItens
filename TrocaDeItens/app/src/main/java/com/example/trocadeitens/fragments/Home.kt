package com.example.trocadeitens.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trocadeitens.adapters.ItensAdapter
import com.example.trocadeitens.databinding.FragmentHomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var allItems: List<Map<String, Any>> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()

        getAllItems { items ->
            if (items != null) {
                setupRecyclerView(items)
            } else {
                Log.w(TAG, "No items to display.")
            }
        }

        // Configurando botão de pesquisa
        binding.btnSearch.setOnClickListener {
            val query = binding.edtSearch.text.toString()
            val selectedType = binding.spinner.selectedItem.toString()
            filterItems(query, selectedType)
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterItems(binding.edtSearch.text.toString(), parent?.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSpinner() {
        val filterOptions = arrayOf("Itens desejados", "Itens para trocar", "Itens disponíveis")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = binding.spinner
        spinner.adapter = adapter
    }

    private fun filterItems(query: String, selectedType: String) {
        val translatedType = when (selectedType) {
            "Itens desejados" -> "Item desejado"
            "Itens para trocar" -> "Item para troca"
            "Itens disponíveis" -> "Item disponível"
            else -> ""
        }

        val filteredItems = allItems.filter { item ->
            val itemName = item["name"]?.toString() ?: ""
            val itemType = item["type"]?.toString() ?: ""
            val itemCategory = item["category"]?.toString() ?: "" // Adicione esta linha

            val matchesQuery = itemName.contains(query, ignoreCase = true) || itemCategory.contains(query, ignoreCase = true) // Verifique também a categoria
            val matchesType = itemType == translatedType || translatedType.isEmpty()

            matchesQuery && matchesType
        }

        setupRecyclerView(filteredItems)
    }

    private fun setupRecyclerView(items: List<Map<String, Any>>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = ItensAdapter(items) { item ->
            // Handle item click
        }
    }

    private fun getAllItems(callback: (List<Map<String, Any>>?) -> Unit) {
        val auth = Firebase.auth
        val user = auth.currentUser
        val db = Firebase.firestore

        if (user == null) {
            Log.w(TAG, "Usuário não autenticado.")
            callback(null)
            return
        }

        db.collection("users").document(user.uid).collection("items").get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val items = querySnapshot.documents.map { document ->
                        val item = document.data?.toMutableMap() ?: mutableMapOf()
                        item["id"] = document.id
                        item
                    }
                    allItems = items
                    callback(items)
                } else {
                    Log.d(TAG, "No items found.")
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
                callback(null)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "Home"
    }
}
