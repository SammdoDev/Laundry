package com.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.laundry.R
import com.laundry.model_data.model_pelanggan
import com.laundry.pelanggan.tambah_pelanggan
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.appcompat.app.AlertDialog

class adapter_data_pelanggan(
    private val listPelanggan: ArrayList<model_pelanggan>
) : RecyclerView.Adapter<adapter_data_pelanggan.ViewHolder>() {

    lateinit var appContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pelanggan, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    @SuppressLint("MissingInflatedId")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pelanggan = listPelanggan[position]

        // Set data ke tampilan kartu
        holder.tvCARD_PELANGGAN_ID.text = pelanggan.idPelanggan
        holder.tvCARD_PELANGGAN_NAMA.text = pelanggan.namaPelanggan
        holder.tvCARD_PELANGGAN_ALAMAT.text = pelanggan.alamatPelanggan
        holder.tvCARD_PELANGGAN_NOHP.text = pelanggan.noHPPelanggan
        holder.tvCARD_PELANGGAN_TERDAFTAR.text = pelanggan.terdaftar ?: "-" // Tambahan

        // Klik tombol lihat
        holder.btnLihat.setOnClickListener {
            val dialogView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.dialog_mod_pelanggan, null)

            val dialogBuilder = AlertDialog.Builder(holder.itemView.context)
                .setView(dialogView)
                .setCancelable(true)

            val alertDialog = dialogBuilder.create()

            // Temukan semua TextView di dialog
            val tvId = dialogView.findViewById<TextView>(R.id.tvDIALOG_PELANGGAN_ID)
            val tvNama = dialogView.findViewById<TextView>(R.id.tvDIALOG_PELANGGAN_NAMA)
            val tvAlamat = dialogView.findViewById<TextView>(R.id.tvDIALOG_PELANGGAN_ALAMAT)
            val tvNoHP = dialogView.findViewById<TextView>(R.id.tvDIALOG_PELANGGAN_NOHP)
            val tvTerdaftar = dialogView.findViewById<TextView>(R.id.tvDIALOG_PELANGGAN_TERDAFTAR)

            val btEdit = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PELANGGAN_Edit)
            val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PELANGGAN_Hapus)

            // Set data di dialog
            tvId?.text = pelanggan.idPelanggan
            tvNama?.text = pelanggan.namaPelanggan
            tvAlamat?.text = pelanggan.alamatPelanggan
            tvNoHP?.text = pelanggan.noHPPelanggan
            tvTerdaftar?.text = pelanggan.terdaftar ?: "-"

            // Edit data
            btEdit?.setOnClickListener {
                val intent = Intent(holder.itemView.context, tambah_pelanggan::class.java)
                intent.putExtra("idPelanggan", pelanggan.idPelanggan)
                intent.putExtra("namaPelanggan", pelanggan.namaPelanggan)
                intent.putExtra("alamatPelanggan", pelanggan.alamatPelanggan)
                intent.putExtra("noHPPelanggan", pelanggan.noHPPelanggan)
                intent.putExtra("terdaftar", pelanggan.terdaftar)
                holder.itemView.context.startActivity(intent)
                alertDialog.dismiss()
            }

            // Hapus data
            btHapus?.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Yakin ingin menghapus data ini?")
                    .setPositiveButton("Ya") { _, _ ->
                        val dbRef = FirebaseDatabase.getInstance()
                            .getReference("Pelanggan")
                            .child(pelanggan.idPelanggan ?: "")

                        dbRef.removeValue().addOnSuccessListener {
                            listPelanggan.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, listPelanggan.size)
                            Toast.makeText(holder.itemView.context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                            alertDialog.dismiss()
                        }.addOnFailureListener {
                            Toast.makeText(holder.itemView.context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            alertDialog.show()
        }
    }

    override fun getItemCount(): Int = listPelanggan.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCARD_PELANGGAN_ID: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_ID)
        val tvCARD_PELANGGAN_NAMA: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_nama)
        val tvCARD_PELANGGAN_ALAMAT: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_alamat)
        val tvCARD_PELANGGAN_NOHP: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_nohp)
        val tvCARD_PELANGGAN_TERDAFTAR: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_terdaftar) // ← Tambahan
        val cvCARD_PELANGGAN: CardView = itemView.findViewById(R.id.cvCARD_PELANGGAN)
        val btnHubungi: Button = itemView.findViewById(R.id.btHubungiPelanggan)
        val btnLihat: Button = itemView.findViewById(R.id.btLihatPelanggan)
    }
}
