package com.example.trocadeitens

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.trocadeitens.databinding.ActivityItemInterfaceBinding
import com.example.trocadeitens.databinding.ActivityRegisterUserBinding

class ItemInterfaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemInterfaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemInterfaceBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val itemName = intent.getStringExtra("itemName")
        val itemDesc = intent.getStringExtra("itemDescription")
        val itemCategory = intent.getStringExtra("itemCategory")
        val userEmail = intent.getStringExtra("userEmail")
        val itemType = intent.getStringExtra("itemType")
        val imageUrl = intent.getStringExtra("imageUrl")

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .error(binding.imgViewItem)
                .into(binding.imgViewItem)
        } else {
            Glide.with(this)
                .load(R.drawable.default_image)  // Substitua pelo recurso da imagem padrão
                .circleCrop()
                .into(binding.imgViewItem)
        }

        binding.txtItemName.text = itemName
        binding.txtCategory.text = itemCategory
        binding.txtItemEmail.text = userEmail
        binding.txtType. text = itemType
        binding.txtDescription.text = itemDesc
    }
}