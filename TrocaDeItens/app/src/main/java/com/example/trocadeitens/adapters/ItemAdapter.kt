package com.example.trocadeitens.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trocadeitens.R

class ItensAdapter(private val itens: List<Map<String, Any>>, private val onItemClick: (Map<String, Any>) -> Unit) :
    RecyclerView.Adapter<ItensAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemDesc: TextView = itemView.findViewById(R.id.itemDesc)
        val categoryTag: TextView = itemView.findViewById(R.id.categoryTag)
        val itemPhoto: ImageView = itemView.findViewById(R.id.itemPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_object, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val item = itens[position]
        holder.itemName.text = item["name"] as? String ?: "Unknown Name"
        holder.itemDesc.text = item["description"] as? String ?: "Unknown Description"
        holder.categoryTag.text = item["category"] as? String ?: "None"

        // Carregar a imagem usando Glide
        val imageUrl = item["imageUrl"] as? String

        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.itemPhoto)
        }

        // Configurar o clique no item
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return itens.size
    }
}
