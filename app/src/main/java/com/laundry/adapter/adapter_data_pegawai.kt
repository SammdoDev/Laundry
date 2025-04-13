package com.adapter

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
import com.laundry.model_data.model_pegawai
import com.laundry.pegawai.tambah_pegawai
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.appcompat.app.AlertDialog


class adapter_data_pegawai(private val listpegawai: ArrayList<model_pegawai>) :
    RecyclerView.Adapter<adapter_data_pegawai.ViewHolder>() {

    lateinit var appContext : Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listpegawai[position]
        databaseReference = FirebaseDatabase.getInstance().getReference("pegawai")


        holder.tvCardPegawaiId.text = "ID Pegawai  : ${item.idPegawai}"
        holder.tvnamapegawai.text = item.namaPegawai
        holder.tvalamatpegawai.text = "Alamat  : ${item.alamatPegawai}"
        holder.tvnohppegawai.text = "Telepon  : ${item.noHPPegawai}"
        holder.tvcabangpegawai.text = "Cabang  : ${item.idCabang}"
        holder.tvterdaftarpegawai.text = item.terdaftar
        holder.cardpegawai.setOnClickListener {
            val intent = Intent(appContext, tambah_pegawai::class.java)
            intent.putExtra("judul",  "Edit Pegawai")
            intent.putExtra("idPegawai", item.idPegawai)
            intent.putExtra("namaPegawai", item.namaPegawai)
            intent.putExtra("noHPPegawai", item.noHPPegawai)
            intent.putExtra("alamatPegawai", item.alamatPegawai)
            intent.putExtra("cabangPegawai", item.idCabang)
            appContext.startActivity(intent)
        }
        holder.btHubungipegawai.setOnClickListener {
        }
        holder.btLihatpegawai.setOnClickListener {
            val dialogView = LayoutInflater.from(appContext).inflate(R.layout.dialog_mod_pegawai, null)

            val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(appContext)
                .setView(dialogView)


            val alertDialog = dialogBuilder.create()
            alertDialog.show()

            // Ambil elemen dari layout dialog
            val tvisiidpegawai = dialogView.findViewById<TextView>(R.id.tvid)
            val tvisinamapegawai = dialogView.findViewById<TextView>(R.id.tvisinamapegawai)
            val tvisialamatpegawai = dialogView.findViewById<TextView>(R.id.tvisialamatpegawai)
            val tvisinohppegawai = dialogView.findViewById<TextView>(R.id.tvisinohppegawai)
            val tvisicabangpegawai = dialogView.findViewById<TextView>(R.id.tvisicabangpegawai)
            val tvisiterdaftarpegawai = dialogView.findViewById<TextView>(R.id.tvisiterdaftarpegawai)
            val buttonsuntingpegawai = dialogView.findViewById<Button>(R.id.buttonsuntingpegawai)
            val buttonhapuspegawai = dialogView.findViewById<Button>(R.id.buttonhapuspegawai)

            // Set data ke dalam dialog
            tvisiidpegawai.text = item.idPegawai
            tvisinamapegawai.text = item.namaPegawai
            tvisialamatpegawai.text = item.alamatPegawai
            tvisinohppegawai.text = item.noHPPegawai
            tvisicabangpegawai.text = item.idCabang
            tvisiterdaftarpegawai.text = item.terdaftar

            // Tombol "Sunting" membuka halaman Edit Pegawai
            buttonsuntingpegawai.setOnClickListener {
                val intent = Intent(appContext, tambah_pegawai::class.java)
                intent.putExtra("judul", "Edit Pegawai")
                intent.putExtra("idPegawai", item.idPegawai)
                intent.putExtra("namaPegawai", item.namaPegawai)
                intent.putExtra("noHPPegawai", item.noHPPegawai)
                intent.putExtra("alamatPegawai", item.alamatPegawai)
                intent.putExtra("cabangPegawai", item.idCabang)
                appContext.startActivity(intent)
                alertDialog.dismiss() // Tutup dialog setelah klik
            }

            // Tombol "Hapus" untuk menghapus pegawai (bisa ditambahkan logika Firebase)
            buttonhapuspegawai.setOnClickListener {
                // Contoh: Konfirmasi sebelum menghapus
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin menghapus pegawai ini?")
                    .setPositiveButton("Hapus") { _, _ ->
                        val idPegawai = item.idPegawai

                        // Pastikan ID Pegawai tidak null atau kosong
                        if (idPegawai.isNullOrEmpty()) {
                            Toast.makeText(holder.itemView.context, "ID Pegawai tidak valid!", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        // Inisialisasi database jika belum dilakukan
                        databaseReference = FirebaseDatabase.getInstance().getReference("pegawai")

                        // Hapus data dari Firebase berdasarkan ID
                        databaseReference.child(idPegawai).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(holder.itemView.context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                                listpegawai.removeAt(position) // Hapus dari list lokal
                                notifyItemRemoved(position) // Perbarui tampilan RecyclerView
                                alertDialog.dismiss() // Tutup dialog
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(holder.itemView.context, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int {
        return listpegawai.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardpegawai: CardView = itemView.findViewById(R.id.cvCARD_PEGAWAI)
        val tvCardPegawaiId: TextView = itemView.findViewById(R.id.tvCARD_PEGAWAI_ID)
        val tvnamapegawai: TextView = itemView.findViewById(R.id.tvCARD_PEGAWAI_nama)
        val tvnohppegawai: TextView = itemView.findViewById(R.id.tvCARD_PEGAWAI_nohp)
        val tvalamatpegawai: TextView = itemView.findViewById(R.id.tvCARD_PEGAWAI_alamat)
        val tvcabangpegawai: TextView = itemView.findViewById(R.id.tvCARD_PEGAWAI_cabang)
        val tvterdaftarpegawai: TextView = itemView.findViewById(R.id.tvTERDAFTAR)
        val btHubungipegawai: Button = itemView.findViewById(R.id.btHubungiPegawai)
        val btLihatpegawai: Button = itemView.findViewById(R.id.btLihatPegawai)
    }
}
