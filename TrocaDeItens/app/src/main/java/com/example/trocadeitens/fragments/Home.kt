package com.example.trocadeitens.fragments

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.trocadeitens.ARG_PARAM1
//import com.example.trocadeitens.ARG_PARAM2
import com.example.trocadeitens.adapters.ItensAdapter
import com.example.trocadeitens.databinding.FragmentHomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [Home.newInstance] factory method to
 * create an instance of this fragment.
 */
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

        // Set up RecyclerView
        getAllItems { items ->
            if (items != null) {
                setupRecyclerView(items)
            } else {
                Log.w(TAG, "No books to display.")
            }
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterItems(binding.edtSearch.text.toString(), parent?.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where no filter is selected if necessary
            }
        }
    }

    private fun setupSpinner() {
        val filterOptions = arrayOf("All", "Trocar", "Desejado") // Add your filter options here
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, filterOptions)
        binding.spinner.adapter = adapter
    }

    private fun filterItems(query: String, category: String) {
        val filteredItems = allItems.filter { item ->
            val matchesQuery = item["name"]?.toString()?.contains(query, ignoreCase = true) ?: false
            val matchesCategory = category == "Trocar" || item["category"]?.toString() == category
            matchesQuery && matchesCategory
        }
        setupRecyclerView(filteredItems)
    }

    private fun setupRecyclerView(items: List<Map<String, Any>>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = ItensAdapter(items) { item ->
            // Handle item click
            // val intent = Intent(context, BookInterface::class.java)
            // startActivity(intent)
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

        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching books", e)
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