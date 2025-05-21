package com.laundry.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.laundry.R
import com.laundry.model_data.model_tambahan

class adapter_pilih_tambahan(private val listTambahan: MutableList<model_tambahan>) :
    RecyclerView.Adapter<adapter_pilih_tambahan.ViewHolder>() {

    private val TAG = "adapter_pilih_tambahan"
    lateinit var appContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pilih_tambahan, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nomor = position + 1
        val item = listTambahan[position]

        holder.tvID.text = "[$nomor]"
        holder.tvNama.text = item.namaTambahan ?: "Tidak ada nama"
        holder.tvHarga.text = "Rp ${item.hargaTambahan ?: "0"}"

        holder.cvCARD.setOnClickListener {
            val intent = Intent().apply {
                putExtra("idTambahan", item.idTambahan)
                putExtra("namaTambahan", item.namaTambahan)
                putExtra("hargaTambahan", item.hargaTambahan)
            }
            (appContext as Activity).setResult(Activity.RESULT_OK, intent)
            (appContext as Activity).finish()
        }
    }

    override fun getItemCount(): Int = listTambahan.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvID: TextView = itemView.findViewById(R.id.tvCARD_PILIH_TAMBAHAN_ID)
        val tvNama: TextView = itemView.findViewById(R.id.tvCARD_PILIH_TAMBAHAN_NAMA)
        val tvHarga: TextView = itemView.findViewById(R.id.tvCARD_PILIH_TAMBAHAN_HARGA)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_PILIH_TAMBAHAN)
    }
}
