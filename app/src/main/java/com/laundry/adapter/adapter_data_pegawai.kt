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
import com.laundry.model_data.model_pegawai
import com.laundry.pegawai.tambah_pegawai
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.appcompat.app.AlertDialog

class adapter_data_pegawai(
    private val listPegawai: ArrayList<model_pegawai>
) : RecyclerView.Adapter<adapter_data_pegawai.ViewHolder>() {

    lateinit var appContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    @SuppressLint("MissingInflatedId")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pegawai = listPegawai[position]

        // Set data ke tampilan kartu
        holder.tvCARD_PEGAWAI_ID.text = pegawai.idPegawai
        holder.tvCARD_PEGAWAI_nama.text = pegawai.namaPegawai
        holder.tvCARD_PEGAWAI_alamat.text = pegawai.alamatPegawai
        holder.tvCARD_PEGAWAI_nohp.text = pegawai.noHPPegawai
        holder.tvTERDAFTAR.text = pegawai.terdaftar ?: "-"
        holder.tvCabang.text = pegawai.idCabang ?: "-"

        // Klik tombol lihat
        holder.btnLihat.setOnClickListener {
            val dialogView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.dialog_mod_pegawai, null)

            val dialogBuilder = AlertDialog.Builder(holder.itemView.context)
                .setView(dialogView)
                .setCancelable(true)

            val alertDialog = dialogBuilder.create()

            // Temukan semua TextView di dialog
            val tvId = dialogView.findViewById<TextView>(R.id.tvDIALOG_PEGAWAI_ID)
            val tvNama = dialogView.findViewById<TextView>(R.id.tvDIALOG_PEGAWAI_NAMA)
            val tvAlamat = dialogView.findViewById<TextView>(R.id.tvDIALOG_PEGAWAI_ALAMAT)
            val tvNoHP = dialogView.findViewById<TextView>(R.id.tvDIALOG_PEGAWAI_NOHP)
            val tvCabang = dialogView.findViewById<TextView>(R.id.tvDIALOG_PEGAWAI_CABANG)

            val btEdit = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PEEGAWAI_Edit)
            val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PEGAWAI_Hapus)

            // Set data di dialog
            tvId?.text = pegawai.idPegawai
            tvNama?.text = pegawai.namaPegawai
            tvAlamat?.text = pegawai.alamatPegawai
            tvNoHP?.text = pegawai.noHPPegawai
            tvCabang?.text = pegawai.idCabang

            // Edit data dari dialog
            btEdit?.setOnClickListener {
                val intent = Intent(holder.itemView.context, tambah_pegawai::class.java)
                intent.putExtra("idPegawai", pegawai.idPegawai)
                intent.putExtra("namaPegawai", pegawai.namaPegawai)
                intent.putExtra("alamatPegawai", pegawai.alamatPegawai)
                intent.putExtra("noHPPegawai", pegawai.noHPPegawai)
                intent.putExtra("idCabang", pegawai.idCabang)
                intent.putExtra("judul", "Edit Pegawai")
                intent.putExtra("fromDialog", true) // Tambahkan flag fromDialog
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
                            .getReference("Pegawai")
                            .child(pegawai.idPegawai ?: "")

                        dbRef.removeValue().addOnSuccessListener {
                            listPegawai.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, listPegawai.size)
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

        // Klik pada card utama
        holder.cvCARD_PEGAWAI.setOnClickListener {
            val intent = Intent(holder.itemView.context, tambah_pegawai::class.java)
            intent.putExtra("idPegawai", pegawai.idPegawai)
            intent.putExtra("namaPegawai", pegawai.namaPegawai)
            intent.putExtra("alamatPegawai", pegawai.alamatPegawai)
            intent.putExtra("noHPPegawai", pegawai.noHPPegawai)
            intent.putExtra("idCabang", pegawai.idCabang)
            intent.putExtra("judul", "Edit Pegawai")
            intent.putExtra("fromDialog", false) // Tidak dari dialog mode
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = listPegawai.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCARD_PEGAWAI_ID: TextView = itemView.findViewById(R.id.tvCARD_PEGAWAI_ID)
        val tvCARD_PEGAWAI_nama: TextView = itemView.findViewById(R.id.tvCARD_PEGAWAI_nama)
        val tvCARD_PEGAWAI_alamat: TextView = itemView.findViewById(R.id.tvCARD_PEGAWAI_alamat)
        val tvCARD_PEGAWAI_nohp: TextView = itemView.findViewById(R.id.tvCARD_PEGAWAI_nohp)
        val tvTERDAFTAR: TextView = itemView.findViewById(R.id.tvTERDAFTAR)
        val tvCabang: TextView = itemView.findViewById(R.id.tvCARD_PEGAWAI_cabang)
        val cvCARD_PEGAWAI: CardView = itemView.findViewById(R.id.cvCARD_PEGAWAI)
        val btnHubungi: Button = itemView.findViewById(R.id.btHubungiPegawai)
        val btnLihat: Button = itemView.findViewById(R.id.btLihatPegawai)
    }
}