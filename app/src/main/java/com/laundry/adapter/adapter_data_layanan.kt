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
import com.laundry.layanan.tambah_layanan
import com.laundry.model_data.model_layanan

class adapter_data_layanan(private val layananList: ArrayList<model_layanan>) :
    RecyclerView.Adapter<adapter_data_layanan.ViewHolder>() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("layanan")

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvCardLayanan: CardView = itemView.findViewById(R.id.cvCARD_LAYANAN)
        val tvIdLayanan: TextView = itemView.findViewById(R.id.tvCARD_LAYANAN_ID)
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvCARD_LAYANAN_NAMA)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvCARD_LAYANAN_HARGA)
        val btHapus: Button = itemView.findViewById(R.id.btHapusLayanan)
        val btEdit: Button = itemView.findViewById(R.id.btEditLayanan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Gunakan holder.adapterPosition untuk mendapatkan posisi yang akurat
        val currentPosition = holder.adapterPosition
        if (currentPosition == RecyclerView.NO_POSITION) return

        val layanan = layananList[currentPosition]

        // Set data ke views
        holder.tvIdLayanan.text = layanan.idLayanan ?: "-"
        holder.tvNamaLayanan.text = layanan.namaLayanan ?: "-"
        holder.tvHargaLayanan.text = "Rp ${formatRupiah(layanan.harga ?: "0")}"

        // Handle tombol edit
        holder.btEdit.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, tambah_layanan::class.java).apply {
                putExtra("ID_LAYANAN", layanan.idLayanan)
                putExtra("NAMA_LAYANAN", layanan.namaLayanan)
                putExtra("HARGA_LAYANAN", layanan.harga)
            }
            context.startActivity(intent)
        }

        // Handle tombol hapus
        holder.btHapus.setOnClickListener {
            // Gunakan holder.adapterPosition untuk mendapatkan posisi saat ini
            val deletePosition = holder.adapterPosition
            if (deletePosition != RecyclerView.NO_POSITION) {
                showDeleteDialog(holder.itemView.context, layanan, deletePosition)
            }
        }
    }

    override fun getItemCount(): Int = layananList.size

    private fun showDeleteDialog(context: Context, layanan: model_layanan, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus layanan '${layanan.namaLayanan}'?")
            .setPositiveButton("Ya") { dialog, _ ->
                dialog.dismiss()
                deleteLayanan(context, layanan, position)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun deleteLayanan(context: Context, layanan: model_layanan, position: Int) {
        // Validasi posisi sebelum menghapus
        if (position < 0 || position >= layananList.size) {
            Toast.makeText(context, "Error: Posisi item tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val idLayanan = layanan.idLayanan
        if (idLayanan.isNullOrEmpty()) {
            Toast.makeText(context, "Error: ID layanan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Hapus dari Firebase
        myRef.child(idLayanan).removeValue()
            .addOnSuccessListener {
                try {
                    // Double check apakah posisi masih valid setelah operasi Firebase
                    if (position >= 0 && position < layananList.size) {
                        layananList.removeAt(position)
                        notifyItemRemoved(position)

                        // Update range hanya jika diperlukan
                        if (position < layananList.size) {
                            notifyItemRangeChanged(position, layananList.size - position)
                        }

                        // Jika list kosong, notify dataset changed
                        if (layananList.isEmpty()) {
                            notifyDataSetChanged()
                        }
                    }
                    Toast.makeText(context, "Layanan berhasil dihapus", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // Jika terjadi error saat update adapter, refresh seluruh data
                    notifyDataSetChanged()
                    Toast.makeText(context, "Layanan dihapus, data direfresh", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Gagal menghapus layanan: ${error.message}", Toast.LENGTH_SHORT).show()
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

    // Method untuk refresh data dari luar adapter jika diperlukan
    fun refreshData(newData: ArrayList<model_layanan>) {
        layananList.clear()
        layananList.addAll(newData)
        notifyDataSetChanged()
    }
}