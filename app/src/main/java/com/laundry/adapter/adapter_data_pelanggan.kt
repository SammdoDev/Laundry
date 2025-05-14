package com.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.laundry.R
import com.laundry.model_data.model_pelanggan
import com.laundry.pelanggan.tambah_pelanggan
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.appcompat.app.AlertDialog


class adapter_data_pelanggan(private val listpelanggan: ArrayList<model_pelanggan>) :
    RecyclerView.Adapter<adapter_data_pelanggan.ViewHolder>() {

    lateinit var appContext : Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pelanggan, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    @SuppressLint("MissingInflatedId")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listpelanggan[position]
        databaseReference = FirebaseDatabase.getInstance().getReference("pelanggan")


        holder.tvCardpelangganId.text = "${item.idPelanggan}"
        holder.tvnamapelanggan.text = item.namaPelanggan
        holder.tvalamatpelanggan.text = "${item.alamatPelanggan}"
        holder.tvnohppelanggan.text = "${item.noHPPelanggan}"
        holder.tvterdaftarpelanggan.text = item.terdaftar
        holder.cardpelanggan.setOnClickListener {
            val intent = Intent(appContext, tambah_pelanggan::class.java)
            intent.putExtra("judul",  "Edit pelanggan")
            intent.putExtra("idPelanggan", item.idPelanggan)
            intent.putExtra("namaPelanggan", item.namaPelanggan)
            intent.putExtra("noHPPelanggan", item.noHPPelanggan)
            intent.putExtra("alamatPelanggan", item.alamatPelanggan)
            appContext.startActivity(intent)
        }
        holder.btHubungipelanggan.setOnClickListener {
        }
        // Klik tombol lihat
        holder.btLihatpelanggan.setOnClickListener {
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

            // Cek null sebelum setText
            tvId?.text = item.idPelanggan
            tvNama?.text = item.namaPelanggan
            tvAlamat?.text = item.alamatPelanggan
            tvNoHP?.text = item.noHPPelanggan
            tvTerdaftar?.text = item.terdaftar // opsional

            btEdit?.setOnClickListener {
                val intent = Intent(holder.itemView.context, tambah_pelanggan::class.java)
                intent.putExtra("idPelanggan", item.idPelanggan)
                intent.putExtra("namaPelanggan", item.namaPelanggan)
                intent.putExtra("alamatPelanggan", item.alamatPelanggan)
                intent.putExtra("noHpPelanggan", item.noHPPelanggan)
                intent.putExtra("idCabang", item.terdaftar)
                holder.itemView.context.startActivity(intent)
                alertDialog.dismiss()
            }

            btHapus?.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Yakin ingin menghapus data ini?")
                    .setPositiveButton("Ya") { _, _ ->
                        val dbRef = FirebaseDatabase.getInstance()
                            .getReference("Pelanggan")
                            .child(item.idPelanggan ?: "")

                        dbRef.removeValue().addOnSuccessListener {
                            listpelanggan.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, listpelanggan.size)
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

    override fun getItemCount(): Int {
        return listpelanggan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardpelanggan: CardView = itemView.findViewById(R.id.cvCARD_PELANGGAN)
        val tvCardpelangganId: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_ID)
        val tvnamapelanggan: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_nama)
        val tvnohppelanggan: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_nohp)
        val tvalamatpelanggan: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_alamat)
        val tvterdaftarpelanggan: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_terdaftar)
        val btHubungipelanggan: Button = itemView.findViewById(R.id.btHubungiPelanggan)
        val btLihatpelanggan: Button = itemView.findViewById(R.id.btLihatPelanggan)
    }
}
