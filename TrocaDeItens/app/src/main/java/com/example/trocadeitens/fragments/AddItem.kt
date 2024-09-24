package com.example.trocadeitens.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.trocadeitens.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.ktx.storage
import java.util.*

class AddItem : Fragment() {

    private lateinit var itemNameEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var visibilityRadioGroup: RadioGroup
    private lateinit var itemImageView: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var addItemButton: Button
    private var itemImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            itemImageUri = it
            itemImageView.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_item, container, false)

        itemNameEditText = view.findViewById(R.id.itemNameEditText)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        typeSpinner = view.findViewById(R.id.typeSpinner)
        visibilityRadioGroup = view.findViewById(R.id.visibilityRadioGroup)
        itemImageView = view.findViewById(R.id.itemImageView)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        addItemButton = view.findViewById(R.id.addItemButton)

        val categories = arrayOf("Eletrôdomesticos", "Eletrônicos", "Livros", "Moveis", "Roupas")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        val typeOptions = arrayOf("Item disponível", "Item para troca", "Item desejado")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        itemImageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        addItemButton.setOnClickListener {
            addItemToDatabase()
        }

        return view
    }

    private fun addItemToDatabase() {
        val itemName = itemNameEditText.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()
        val type = typeSpinner.selectedItem.toString()
        val visibility = when (visibilityRadioGroup.checkedRadioButtonId) {
            R.id.publicRadioButton -> "public"
            R.id.privateRadioButton -> "private"
            else -> ""
        }
        val description = descriptionEditText.text.toString().trim()

        if (itemName.isEmpty() || category.isEmpty() || type.isEmpty() || visibility.isEmpty()) {
            Toast.makeText(context, "Por favor, preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        val firestore = Firebase.firestore
        val itemId = UUID.randomUUID().toString()

        if (itemImageUri != null) {
            val storageReference = Firebase.storage.reference.child("item_images/$itemId")
            storageReference.putFile(itemImageUri!!).addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    saveItemToFirestore(itemId, itemName, category, type, visibility, description, uri.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Falha ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
            }
        } else {
            saveItemToFirestore(itemId, itemName, category, type, visibility, description, null)
        }
    }

    private fun saveItemToFirestore(itemId: String, itemName: String, category: String, type: String, visibility: String, description: String, imageUrl: String?) {
        val item = hashMapOf(
            "id" to itemId,
            "name" to itemName,
            "category" to category,
            "type" to type,
            "visibility" to visibility,
            "description" to description,
            "imageUrl" to imageUrl
        )

        Firebase.firestore.collection("items").document(itemId).set(item)
            .addOnSuccessListener {
                Toast.makeText(context, "Item adicionado com sucesso", Toast.LENGTH_SHORT).show()
                // Limpar campos após adicionar
                itemNameEditText.text.clear()
                descriptionEditText.text.clear()
                itemImageView.setImageResource(R.drawable.ic_add_photo)
                visibilityRadioGroup.clearCheck()
                categorySpinner.setSelection(0)
                typeSpinner.setSelection(0)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Falha ao adicionar item", Toast.LENGTH_SHORT).show()
            }
    }
}