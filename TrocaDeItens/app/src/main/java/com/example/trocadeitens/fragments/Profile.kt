package com.example.trocadeitens.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.trocadeitens.databinding.FragmentProfileBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trocadeitens.LoginActivity
import com.example.trocadeitens.R

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUser()

        // Configura os WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configura o botão de edição
        binding.btnEditUser.setOnClickListener {
            val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
                putExtra("username", binding.nameView.text.toString())
            }
            startActivity(intent)
        }

        // Configura o botão de logout
        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut() // Desloga o usuário do Firebase
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Finaliza a atividade atual para impedir que o usuário volte
        }
    }

    private fun getUser() {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore

        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val imageUrl = document.getString("imageUrl")
                        val username = document.getString("username")
                        val email = document.getString("email")

                        binding.nameView.text = username
                        binding.emailView.text = email

                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .error(binding.userImage)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
