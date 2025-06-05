package com.laundry.transaksi

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.laundry.R
import com.laundry.adapter.adapter_pilih_tambahan
import com.laundry.model_data.model_tambahan
import androidx.appcompat.app.AppCompatDelegate

class data_transaksi : AppCompatActivity() {
    private lateinit var tvPelangganNama: TextView
    private lateinit var tvPelangganNoHP: TextView
    private lateinit var tvLayananNama: TextView
    private lateinit var tvLayananHarga: TextView
    private lateinit var tvJumlahLayanan: TextView
    private lateinit var tvTotalHargaLayanan: TextView
    private lateinit var quantityContainer: LinearLayout
    private lateinit var rvLayananTambahan: RecyclerView
    private lateinit var btnPilihPelanggan: Button
    private lateinit var btnPilihLayanan: Button
    private lateinit var btnTambahan: Button
    private lateinit var btnTambahLayanan: ImageView
    private lateinit var btnKurangLayanan: ImageView

    private val dataListTambahan = mutableListOf<model_tambahan>()
    private lateinit var adapter: adapter_pilih_tambahan

    private val pilih_Pelanggan = 1
    private val pilih_Layanan = 2
    private val pilih_Tambahan = 3

    private var idPelanggan: String = ""
    private var idCabang: String = ""
    private var namaPelanggan: String = ""
    private var noHP: String = ""
    private var idLayanan: String = ""
    private var namaLayanan: String = ""
    private var hargaLayanan: String = ""
    private var hargaLayananInt: Int = 0
    private var jumlahLayanan: Int = 1
    private var idPegawai: String = ""

    private lateinit var sharedPref: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.data_transaksi)

        sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        idCabang = sharedPref.getString("idCabang", null).toString()
        idPegawai = sharedPref.getString("idPegawai", null).toString()

        init()
        FirebaseApp.initializeApp(this)

        setupRecyclerView()
        setupClickListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        tvPelangganNama = findViewById(R.id.tvNamaPelanggan)
        tvPelangganNoHP = findViewById(R.id.tvPelangganNoHP)
        tvLayananNama = findViewById(R.id.tvNamaLayanan)
        tvLayananHarga = findViewById(R.id.tvLayananHarga)
        tvJumlahLayanan = findViewById(R.id.tvJumlahLayanan)
        tvTotalHargaLayanan = findViewById(R.id.tvTotalHargaLayanan)
        quantityContainer = findViewById(R.id.quantityContainer)
        rvLayananTambahan = findViewById(R.id.rvLayananTambahan)
        btnPilihPelanggan = findViewById(R.id.btnPilihPelanggan)
        btnPilihLayanan = findViewById(R.id.btnPilihLayanan)
        btnTambahan = findViewById(R.id.btnTambahan)
        btnTambahLayanan = findViewById(R.id.btnTambahLayanan)
        btnKurangLayanan = findViewById(R.id.btnKurangLayanan)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        rvLayananTambahan.layoutManager = layoutManager
        rvLayananTambahan.setHasFixedSize(true)

        // Adapter untuk data_transaksi dengan tombol hapus dan tidak selectable
        adapter = adapter_pilih_tambahan(
            listTambahan = dataListTambahan,
            onDeleteClick = { position ->
                removeLayananTambahan(position)
            },
            showDeleteButton = true, // Tampilkan tombol hapus
            isSelectable = false // Tidak bisa diklik untuk seleksi
        )
        rvLayananTambahan.adapter = adapter
    }

    private fun setupClickListeners() {
        btnPilihPelanggan.setOnClickListener {
            val intent = Intent(this, pilihPelanggan::class.java)
            startActivityForResult(intent, pilih_Pelanggan)
        }

        btnPilihLayanan.setOnClickListener {
            val intent = Intent(this, pilihLayanan::class.java)
            startActivityForResult(intent, pilih_Layanan)
        }

        btnTambahan.setOnClickListener {
            val intent = Intent(this, pilihLayananTambahan::class.java)
            startActivityForResult(intent, pilih_Tambahan)
        }

        btnTambahLayanan.setOnClickListener {
            jumlahLayanan++
            updateQuantityDisplay()
        }

        btnKurangLayanan.setOnClickListener {
            if (jumlahLayanan > 1) {
                jumlahLayanan--
                updateQuantityDisplay()
            }
        }

// Pada bagian btnProcess.setOnClickListener di data_transaksi.kt
// Ganti kode yang ada dengan ini:

        val btnProcess: Button = findViewById(R.id.btn_process)
        btnProcess.setOnClickListener {
            if (idPelanggan.isEmpty() || idLayanan.isEmpty()) {
                Toast.makeText(this, getString(R.string.msg_pilih_pelanggan_layanan), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, KonfirmasiData::class.java).apply {
                putExtra("namaPelanggan", namaPelanggan)
                putExtra("noHP", noHP)
                putExtra("namaLayanan", namaLayanan)
                putExtra("hargaLayanan", hargaLayanan)
                putExtra("jumlahLayanan", jumlahLayanan) // Data quantity yang sudah ada
                putExtra("hargaLayananInt", hargaLayananInt) // Tambahan: harga per unit
                putExtra("totalHargaLayanan", hargaLayananInt * jumlahLayanan)
                putExtra("layananTambahan", ArrayList(dataListTambahan) as java.io.Serializable)
            }
            startActivity(intent)
        }
    }

    private fun updateQuantityDisplay() {
        tvJumlahLayanan.text = jumlahLayanan.toString()
        val totalHarga = hargaLayananInt * jumlahLayanan
        val context = tvTotalHargaLayanan.context
        tvTotalHargaLayanan.text = context.getString(R.string.label_total_harga, formatRupiah(totalHarga))


        // Enable/disable tombol kurang
        btnKurangLayanan.isEnabled = jumlahLayanan > 1
        btnKurangLayanan.alpha = if (jumlahLayanan > 1) 1.0f else 0.5f
    }

    private fun formatRupiah(amount: Int): String {
        return String.format("%,d", amount).replace(',', '.')
    }

    private fun removeLayananTambahan(position: Int) {
        if (position >= 0 && position < dataListTambahan.size) {
            dataListTambahan.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, dataListTambahan.size)
            Toast.makeText(this, getString(R.string.msg_layanan_tambahan_dihapus), Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pilih_Pelanggan) {
            if (resultCode == RESULT_OK && data != null) {
                idPelanggan = data.getStringExtra("idPelanggan").toString()
                val nama = data.getStringExtra("nama")
                val nomorHP = data.getStringExtra("hp")

                tvPelangganNama.text = getString(R.string.label_nama_pelanggan, nama)
                tvPelangganNoHP.text = getString(R.string.label_no_hp, nomorHP)


                namaPelanggan = nama.toString()
                noHP = nomorHP.toString()
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.msg_batal_pilih_pelanggan), Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == pilih_Layanan) {
            if (resultCode == RESULT_OK && data != null) {
                idLayanan = data.getStringExtra("idLayanan").toString()
                val tempNamaLayanan = data.getStringExtra("namaLayanan")
                val harga = data.getStringExtra("harga")

                tvLayananNama.text = getString(R.string.label_nama_layanan, tempNamaLayanan)
                tvLayananHarga.text = getString(R.string.label_harga, harga)


                namaLayanan = tempNamaLayanan.toString()
                hargaLayanan = harga.toString()

                // Parse harga ke integer untuk perhitungan
                try {
                    hargaLayananInt = harga?.replace("[^0-9]".toRegex(), "")?.toInt() ?: 0
                } catch (e: NumberFormatException) {
                    hargaLayananInt = 0
                }

                // Reset jumlah ke 1 saat pilih layanan baru
                jumlahLayanan = 1

                // Tampilkan quantity container
                quantityContainer.visibility = View.VISIBLE
                updateQuantityDisplay()

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.msg_batal_memilih_layanan), Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == pilih_Tambahan) {
            if (resultCode == RESULT_OK && data != null) {
                val idTambahan = data.getStringExtra("idTambahan").toString()
                val namaTambahan = data.getStringExtra("namaTambahan").toString()
                val hargaTambahan = data.getStringExtra("hargaTambahan").toString()

                // Cek apakah item sudah ada dalam list
                val existingItem = dataListTambahan.find { it.idTambahan == idTambahan }
                if (existingItem == null) {
                    val tambahan = model_tambahan(idTambahan, namaTambahan, hargaTambahan)
                    dataListTambahan.add(tambahan)
                    adapter.notifyItemInserted(dataListTambahan.size - 1)
                    Toast.makeText(this, getString(R.string.msg_tambahan_ditambahkan), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.msg_tambahan_sudah_ada), Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.msg_batal_memilih_tambahan), Toast.LENGTH_SHORT).show()
            }
        }
    }
}