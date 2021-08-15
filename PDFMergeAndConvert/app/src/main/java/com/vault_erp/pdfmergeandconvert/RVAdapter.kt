package com.vault_erp.pdfmergeandconvert

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class RVAdapter(private val context: Context, private val list: ArrayList<Bitmap?>) : RecyclerView.Adapter<RVAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imagePreview = itemView.findViewById<ImageView>(R.id.imagePreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.pdf_viewer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imagePreview.setImageBitmap(list[position])
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    fun updateList(list: ArrayList<Bitmap>) {
        val data = arrayListOf<Bitmap?>()
        data.addAll(list)

        this.list.clear()
        this.list.addAll(data)

        notifyDataSetChanged()
    }
}