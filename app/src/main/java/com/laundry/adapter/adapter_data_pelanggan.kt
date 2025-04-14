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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listpelanggan[position]
        databaseReference = FirebaseDatabase.getInstance().getReference("pelanggan")


        holder.tvCardpelangganId.text = "${item.idPelanggan}"
        holder.tvnamapelanggan.text = item.namaPelanggan
        holder.tvalamatpelanggan.text = "Alamat  : ${item.alamatPelanggan}"
        holder.tvnohppelanggan.text = "Telepon  : ${item.noHPPelanggan}"
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
        holder.btLihatpelanggan.setOnClickListener {
            val dialogView = LayoutInflater.from(appContext).inflate(R.layout.dialog_mod_pelanggan, null)

            val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(appContext)
                .setView(dialogView)


            val alertDialog = dialogBuilder.create()
            alertDialog.show()

            // Ambil elemen dari layout dialog
            val tvisiidpelanggan = dialogView.findViewById<TextView>(R.id.tvid)
            val tvisinamapelanggan = dialogView.findViewById<TextView>(R.id.tvisinamapelanggan)
            val tvisialamatpelanggan = dialogView.findViewById<TextView>(R.id.tvisialamatpelanggan)
            val tvisinohppelanggan = dialogView.findViewById<TextView>(R.id.tvisinohppelanggan)
            val tvisiterdaftarpelanggan = dialogView.findViewById<TextView>(R.id.tvisiterdaftarpelanggan)
            val buttonsuntingpelanggan = dialogView.findViewById<Button>(R.id.buttonsuntingpelanggan)
            val buttonhapuspelanggan = dialogView.findViewById<Button>(R.id.buttonhapuspelanggan)

            // Set data ke dalam dialog
            tvisiidpelanggan.text = item.idPelanggan
            tvisinamapelanggan.text = item.namaPelanggan
            tvisialamatpelanggan.text = item.alamatPelanggan
            tvisinohppelanggan.text = item.noHPPelanggan
            tvisiterdaftarpelanggan.text = item.terdaftar

            // Tombol "Sunting" membuka halaman Edit pelanggan
            buttonsuntingpelanggan.setOnClickListener {
                val intent = Intent(appContext, tambah_pelanggan::class.java)
                intent.putExtra("judul", "Edit pelanggan")
                intent.putExtra("idPelanggan", item.idPelanggan)
                intent.putExtra("namaPelanggan", item.namaPelanggan)
                intent.putExtra("noHPPelanggan", item.noHPPelanggan)
                intent.putExtra("alamatPelanggan", item.alamatPelanggan)
                appContext.startActivity(intent)
                alertDialog.dismiss() // Tutup dialog setelah klik
            }

            // Tombol "Hapus" untuk menghapus pelanggan (bisa ditambahkan logika Firebase)
            buttonhapuspelanggan.setOnClickListener {
                // Contoh: Konfirmasi sebelum menghapus
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin menghapus pelanggan ini?")
                    .setPositiveButton("Hapus") { _, _ ->
                        val idPelanggan = item.idPelanggan

                        // Pastikan ID pelanggan tidak null atau kosong
                        if (idPelanggan.isNullOrEmpty()) {
                            Toast.makeText(holder.itemView.context, "ID pelanggan tidak valid!", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        // Inisialisasi database jika belum dilakukan
                        databaseReference = FirebaseDatabase.getInstance().getReference("pelanggan")

                        // Hapus data dari Firebase berdasarkan ID
                        databaseReference.child(idPelanggan).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(holder.itemView.context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                                listpelanggan.removeAt(position) // Hapus dari list lokal
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
