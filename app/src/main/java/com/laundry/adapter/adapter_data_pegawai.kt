package com.adapter

import com.laundry.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.laundry.model_data.model_pegawai

class adapter_data_pegawai(
    private val listPegawai: ArrayList<model_pegawai>) :
    RecyclerView.Adapter<adapter_data_pegawai.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPegawai[position]
        holder.tvCARD_PEGAWAI_ID.text = item.idPegawai
        holder.tvCARD_PEGAWAI_NAMA.text = item.namaPegawai
        holder.tvCARD_PEGAWAI_ALAMAT.text = item.alamatPegawai
        holder.tvCARD_PEGAWAI_NOHP.text = item.noHPPegawai
        holder.tvCARD_TERDAFTAR.text = item.terdaftar
        holder.tvCARD_PEGAWAI_CABANG.text = item.idCabang
        holder.cvCARD_PEGAWAI.setOnClickListener {
        }
    }

    override fun getItemCount(): Int {
        return listPegawai.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvCARD_PEGAWAI = itemView.findViewById<View>(R.id.cvCARD_PEGAWAI)
        val tvCARD_PEGAWAI_ID = itemView.findViewById<TextView>(R.id.tvCARD_PEGAWAI_ID)
        val tvCARD_PEGAWAI_NAMA = itemView.findViewById<TextView>(R.id.tvCARD_PEGAWAI_nama)
        val tvCARD_PEGAWAI_ALAMAT = itemView.findViewById<TextView>(R.id.tvCARD_PEGAWAI_alamat)
        val tvCARD_PEGAWAI_NOHP = itemView.findViewById<TextView>(R.id.tvCARD_PEGAWAI_nohp)
        val tvCARD_TERDAFTAR = itemView.findViewById<TextView>(R.id.tvTERDAFTAR)
        val tvCARD_PEGAWAI_CABANG = itemView.findViewById<TextView>(R.id.tvCARD_PEGAWAI_cabang)
    }

}