package com.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.laundry.R
import com.laundry.tambahan.tambah_tambahan
import com.laundry.model_data.model_tambahan

class adapter_data_tambahan(private val tambahanList: ArrayList<model_tambahan>) :
    RecyclerView.Adapter<adapter_data_tambahan.ViewHolder>() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("tambahan")

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvCardTambahan: CardView = itemView.findViewById(R.id.cvCARD_TAMBAHAN)
        val tvIdTambahan: TextView = itemView.findViewById(R.id.tvCARD_TAMBAHAN_ID)
        val tvNamaTambahan: TextView = itemView.findViewById(R.id.tvCARD_TAMBAHAN_NAMA)
        val tvHargaTambahan: TextView = itemView.findViewById(R.id.tvCARD_TAMBAHAN_HARGA)
        val btHapus: Button = itemView.findViewById(R.id.btHapusTambahan)
        val btEdit: Button = itemView.findViewById(R.id.btEditTambahan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_tambahan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tambahan = tambahanList[position]

        // Set data ke views
        holder.tvIdTambahan.text = tambahan.idTambahan ?: "-"
        holder.tvNamaTambahan.text = tambahan.namaTambahan ?: "-"
        holder.tvHargaTambahan.text = "Rp ${formatRupiah(tambahan.hargaTambahan ?: "0")}"

        // Handle tombol edit
        holder.btEdit.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, tambah_tambahan::class.java).apply {
                putExtra("ID_TAMBAHAN", tambahan.idTambahan)
                putExtra("NAMA_TAMBAHAN", tambahan.namaTambahan)
                putExtra("HARGA_TAMBAHAN", tambahan.hargaTambahan)
            }
            context.startActivity(intent)
        }

        // Handle tombol hapus
        holder.btHapus.setOnClickListener {
            showDeleteDialog(holder.itemView.context, tambahan, position)
        }
    }

    override fun getItemCount(): Int = tambahanList.size

    private fun showDeleteDialog(context: Context, tambahan: model_tambahan, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus tambahan '${tambahan.namaTambahan}'?")
            .setPositiveButton("Ya") { _, _ ->
                deleteTambahan(context, tambahan, position)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteTambahan(context: Context, tambahan: model_tambahan, position: Int) {
        tambahan.idTambahan?.let { id ->
            myRef.child(id).removeValue()
                .addOnSuccessListener {
                    // Cek apakah position masih valid
                    if (position >= 0 && position < tambahanList.size) {
                        tambahanList.removeAt(position)
                        notifyItemRemoved(position)
                        // Hanya update range jika ada item setelah position yang dihapus
                        if (position < tambahanList.size) {
                            notifyItemRangeChanged(position, tambahanList.size - position)
                        }
                    }
                    Toast.makeText(context, "Tambahan berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(context, "Gagal menghapus tambahan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(context, "ID tambahan tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiah(amount: String?): String {
        if (amount.isNullOrEmpty()) return "0"
        return try {
            val number = amount.toLong()
            String.format("%,d", number).replace(',', '.')
        } catch (e: NumberFormatException) {
            amount
        }
    }
}