package com.example.trocadeitens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.trocadeitens.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var imageUri: Uri? = null

    private val imageContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.userImage.setImageURI(uri)
            Toast.makeText(this, "Imagem selecionada com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Carregar os dados do usuário
        loadUserData()

        // Configurar botão de seleção de imagem
        binding.userImage.setOnClickListener {
            imageContract.launch("image/*")
        }

        // Configurar botão de confirmar
        binding.btnConfirmChanges.setOnClickListener {
            updateUserProfile()
        }

        // Configurar botão de cancelar
        binding.btnCancelChanges.setOnClickListener {
            finish() // Fecha a atividade e volta para a anterior
        }

        // Configurar botão de deletar usuário (opcional)
        binding.btnDeletUser.setOnClickListener {
            deleteUser()
        }
    }

    private fun loadUserData() {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore

        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val imageUrl = document.getString("imageUrl")
                        val username = document.getString("username")

                        binding.nameView.setText(username)

                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .into(binding.userImage)
                        } else {
                            Glide.with(this)
                                .load(R.drawable.default_image)  // Substitua pelo recurso da imagem padrão
                                .circleCrop()
                                .into(binding.userImage)
                        }
                    }
                }
        }
    }

    private fun updateUserProfile() {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore

        if (user != null) {
            val updatedName = binding.nameView.text.toString()
            val newPassword = binding.passwordView.text.toString()

            val userMap = hashMapOf(
                "username" to updatedName
            )

            // Atualizar a imagem no Firebase Storage, se uma nova imagem foi selecionada
            if (imageUri != null) {
                val storageRef = Firebase.storage.reference
                val fileRef = storageRef.child("user_images/${user.uid}.jpg")

                fileRef.putFile(imageUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                            userMap["imageUrl"] = uri.toString()

                            // Atualizar dados do usuário no Firestore e a senha (se fornecida)
                            updateUserData(db, user, userMap, newPassword, updatedName, uri.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao fazer upload da imagem.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Atualizar dados do usuário no Firestore e a senha (se fornecida)
                updateUserData(db, user, userMap, newPassword, updatedName, null)
            }
        }
    }

    private fun updateUserData(db: FirebaseFirestore, user: FirebaseUser, userMap: HashMap<String, String>, newPassword: String, updatedName: String, imageUrl: String?) {
        db.collection("users").document(user.uid)
            .update(userMap as Map<String, Any>)
            .addOnSuccessListener {
                if (newPassword.isNotEmpty()) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                returnToProfile(updatedName, imageUrl)
                            } else {
                                Toast.makeText(this, "Perfil atualizado, mas houve um erro ao mudar a senha.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    returnToProfile(updatedName, imageUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao atualizar perfil.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun returnToProfile(updatedName: String, imageUrl: String?) {
        val intent = Intent()
        intent.putExtra("updatedName", updatedName)
        imageUrl?.let {
            intent.putExtra("updatedImageUrl", it)
        }
        setResult(RESULT_OK, intent)
        finish() // Voltar para a tela de perfil
    }

    private fun deleteUser() {
        val user = Firebase.auth.currentUser

        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Firebase.firestore.collection("users").document(user.uid).delete()
                Toast.makeText(this, "Usuário deletado com sucesso!", Toast.LENGTH_SHORT).show()
                finish() // Finaliza a atividade atual para impedir que o usuário volte
            } else {
                Toast.makeText(this, "Erro ao deletar usuário.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
