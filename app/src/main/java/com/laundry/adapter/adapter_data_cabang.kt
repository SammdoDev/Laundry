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
import com.laundry.cabang.tambah_cabang
import com.laundry.model_data.model_cabang

class adapter_data_cabang(private val cabangList: ArrayList<model_cabang>) :
    RecyclerView.Adapter<adapter_data_cabang.ViewHolder>() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvCardCabang: CardView = itemView.findViewById(R.id.cvCARD_CABANG)
        val tvIdCabang: TextView = itemView.findViewById(R.id.tvCARD_CABANG_ID)
        val tvNamaCabang: TextView = itemView.findViewById(R.id.tvCARD_CABANG_NAMA)
        val tvAlamatCabang: TextView = itemView.findViewById(R.id.tvCARD_CABANG_ALAMAT)
        val tvnoHPCabang: TextView = itemView.findViewById(R.id.tvCARD_CABANG_Telepon)
        val btHapus: Button = itemView.findViewById(R.id.btHapusCabang)
        val btEdit: Button = itemView.findViewById(R.id.btEditCabang)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Gunakan holder.adapterPosition untuk mendapatkan posisi yang akurat
        val currentPosition = holder.adapterPosition
        if (currentPosition == RecyclerView.NO_POSITION) return

        val cabang = cabangList[currentPosition]

        // Set data ke views
        holder.tvIdCabang.text = cabang.idCabang ?: "-"
        holder.tvNamaCabang.text = cabang.namaCabang ?: "-"
        holder.tvAlamatCabang.text = cabang.alamatCabang ?: "-"
        holder.tvnoHPCabang.text = cabang.nomorTelepon ?: "-"

        // Handle tombol edit
        holder.btEdit.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, tambah_cabang::class.java).apply {
                putExtra("ID_CABANG", cabang.idCabang)
                putExtra("NAMA_CABANG", cabang.namaCabang)
                putExtra("ALAMAT_CABANG", cabang.alamatCabang)
                putExtra("NOMOR_TELEPON", cabang.nomorTelepon)
            }
            context.startActivity(intent)
        }

        // Handle tombol hapus
        holder.btHapus.setOnClickListener {
            // Gunakan holder.adapterPosition untuk mendapatkan posisi saat ini
            val deletePosition = holder.adapterPosition
            if (deletePosition != RecyclerView.NO_POSITION) {
                showDeleteDialog(holder.itemView.context, cabang, deletePosition)
            }
        }
    }

    override fun getItemCount(): Int = cabangList.size

    private fun showDeleteDialog(context: Context, cabang: model_cabang, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus cabang '${cabang.namaCabang}'?")
            .setPositiveButton("Ya") { dialog, _ ->
                dialog.dismiss()
                deleteCabang(context, cabang, position)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun deleteCabang(context: Context, cabang: model_cabang, position: Int) {
        // Validasi posisi sebelum menghapus
        if (position < 0 || position >= cabangList.size) {
            Toast.makeText(context, context.getString(R.string.msg_error_posisi_item), Toast.LENGTH_SHORT).show()
            return
        }

        val idCabang = cabang.idCabang
        if (idCabang.isNullOrEmpty()) {
            Toast.makeText(context, context.getString(R.string.msg_error_id_cabang), Toast.LENGTH_SHORT).show()
            return
        }

        // Hapus dari Firebase
        myRef.child(idCabang).removeValue()
            .addOnSuccessListener {
                try {
                    // Double check apakah posisi masih valid setelah operasi Firebase
                    if (position >= 0 && position < cabangList.size) {
                        cabangList.removeAt(position)
                        notifyItemRemoved(position)

                        // Update range hanya jika diperlukan
                        if (position < cabangList.size) {
                            notifyItemRangeChanged(position, cabangList.size - position)
                        }

                        // Jika list kosong, notify dataset changed
                        if (cabangList.isEmpty()) {
                            notifyDataSetChanged()
                        }
                    }
                    Toast.makeText(context, context.getString(R.string.msg_cabang_berhasil_dihapus), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // Jika terjadi error saat update adapter, refresh seluruh data
                    notifyDataSetChanged()
                    Toast.makeText(context, context.getString(R.string.msg_cabang_dihapus_refresh), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, context.getString(R.string.msg_gagal_hapus_cabang, error.message), Toast.LENGTH_SHORT).show()
            }
    }

    // Method untuk refresh data dari luar adapter jika diperlukan
    fun refreshData(newData: ArrayList<model_cabang>) {
        cabangList.clear()
        cabangList.addAll(newData)
        notifyDataSetChanged()
    }
}