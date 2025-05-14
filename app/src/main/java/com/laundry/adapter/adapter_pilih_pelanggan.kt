package com.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.laundry.R
import com.laundry.model_data.model_pelanggan
import com.laundry.transaksi.data_transaksi
import android.widget.TextView
import com.google.firebase.database.DatabaseReference



class adapter_pilih_pelanggan(
    private val pelangganList: ArrayList<model_pelanggan>) :
    RecyclerView.Adapter<adapter_pilih_pelanggan.ViewHolder>() {

    lateinit var appContext: Context
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

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val nomor = position + 1
        val item = pelangganList[position]
        holder.tvID.text = "nomor"
        holder.tvNama.text = item.namaPelanggan
        holder.tvAlamat.text = "Alamat : {item.alamatPelanggan}"
        holder.tvNoHP.text = "No HP : {item.noHPPelanggan}"

        holder.cvCARD.setOnClickListener {
            val intent = Intent(appContext, data_transaksi::class.java)
            intent.putExtra("idPelanggan", item.idPelanggan)
            intent.putExtra("nama", item.namaPelanggan)
            intent.putExtra("noHP", item.noHPPelanggan)
            (appContext as Activity).setResult(Activity.RESULT_OK, intent)
            (appContext as Activity).finish()
        }
    }

    override fun getItemCount(): Int {
        return pelangganList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvID = itemView.findViewById<TextView>(R.id.tvCARD_PILIHPELANGGAN_ID)
        val tvNama = itemView.findViewById<TextView>(R.id.tvCARD_PILIHPELANGGAN_nama)
        val tvAlamat = itemView.findViewById<TextView>(R.id.tvCARD_PILIHPELANGGAN_alamat)
        val tvNoHP = itemView.findViewById<TextView>(R.id.tvCARD_PILIHPELANGGAN_nohp)
        val cvCARD = itemView.findViewById<View>(R.id.cvCARD_PILIHPELANGGAN)
    }
}
