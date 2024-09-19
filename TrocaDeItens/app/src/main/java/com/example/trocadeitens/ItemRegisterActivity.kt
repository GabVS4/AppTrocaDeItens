package com.example.trocadeitens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.trocadeitens.databinding.ActivityItemRegisterBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ItemRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemRegisterBinding
    private var selectedCategory: String = ""
    private var imageUri: Uri? = null

    private val cameraPermissionCode = 1
    private val galleryPermissionCode = 2
    private val cameraRequestCode = 3
    private val galleryRequestCode = 4

    private val getImageFromGallery: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                imageUri = data?.data
                binding.imgItem.setImageURI(imageUri)
            }
        }

    private val takePhoto: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                binding.imgItem.setImageURI(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicitar permissões se não concedidas
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE), galleryPermissionCode)
        }

        // Configurando o Spinner de categorias
        setupCategorySpinner()

        // Configurações dos botões de câmera e galeria
        binding.btnChooseFromGallery.setOnClickListener {
            openGallery()
        }

        binding.btnTakePhoto.setOnClickListener {
            openCamera()
        }

        // Quando o botão de registrar item é clicado
        binding.btnRegisterItem.setOnClickListener {
            val itemName = binding.edtItemName.text.toString()
            val isVisible = binding.checkboxVisibility.isChecked

            if (itemName.isBlank() || selectedCategory.isBlank()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                registerItem(itemName, selectedCategory, isVisible)
            }
        }
    }

    // Configura o Spinner com as opções de categorias
    private fun setupCategorySpinner() {
        val categories = listOf("Eletrônicos", "Roupas", "Livros", "Móveis")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // Quando uma categoria é selecionada
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = ""
            }
        }
    }

    // Função para abrir a galeria
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImageFromGallery.launch(intent)
    }

    // Função para abrir a câmera
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, cameraRequestCode)
        }
    }

    // Função para registrar o item
    private fun registerItem(itemName: String, category: String, isVisible: Boolean) {
        val currentUser = Firebase.auth.currentUser

        if (currentUser != null) {
            val dbHelper = DatabaseHelper(this)
            val userId = dbHelper.addUser(currentUser.displayName ?: "Anonymous", currentUser.email ?: "")

            // Adiciona o item e mostra Toast
            val itemId = dbHelper.addItem(itemName, category, isVisible, userId)

            // Sincroniza a visibilidade com o Firebase
            syncItemVisibilityWithFirebase(itemId, isVisible)

            Toast.makeText(this, "Item cadastrado com sucesso no banco de dados!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    // Função para sincronizar a visibilidade do item no Firebase
    private fun syncItemVisibilityWithFirebase(itemId: Long, visible: Boolean) {
        val db = Firebase.firestore
        val itemRef = db.collection("items").document(itemId.toString())

        if (visible) {
            itemRef.update("visible", true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Item visível no Firebase!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao tornar item visível no Firebase", Toast.LENGTH_SHORT).show()
                }
        } else {
            itemRef.update("visible", false)
                .addOnSuccessListener {
                    Toast.makeText(this, "Item invisível no Firebase!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao tornar item invisível no Firebase", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Função para lidar com a resposta das permissões solicitadas
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            galleryPermissionCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissão para armazenamento concedida", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permissão para armazenamento negada", Toast.LENGTH_SHORT).show()
                }
            }
            cameraPermissionCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissão para câmera concedida", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permissão para câmera negada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
