package com.laundry.invoice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.laundry.laporan.data_laporan
import com.laundry.R
import com.laundry.model_data.StatusLaporan
import com.laundry.model_data.model_laporan
import com.laundry.model_data.model_tambahan
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class Invoice : AppCompatActivity() {

    // Header views
    private lateinit var tvBusinessName: TextView
    private lateinit var tvBranch: TextView

    // Transaction Details views
    private lateinit var tvTransactionId: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvCustomer: TextView
    private lateinit var tvCustomerPhone: TextView
    private lateinit var tvEmployee: TextView
    private lateinit var tvPaymentMethod: TextView

    // Main Service views
    private lateinit var tvMainService: TextView
    private lateinit var tvMainServiceQuantity: TextView
    private lateinit var tvMainServicePricePerUnit: TextView
    private lateinit var tvMainServicePrice: TextView

    // Additional Services views
    private lateinit var tvAdditionalServicesHeader: TextView
    private lateinit var rvAdditionalServices: RecyclerView
    private lateinit var tvSubtotalAdditional: TextView

    // Total view
    private lateinit var tvTotal: TextView

    // Buttons
    private lateinit var btnWhatsapp: Button
    private lateinit var btnPrint: Button

    // Data variables
    private var namaPelanggan: String = ""
    private var nomorHp: String = ""
    private var namaLayanan: String = ""
    private var hargaLayanan: Int = 0
    private var jumlahLayanan: Int = 1
    private var totalHargaLayanan: Int = 0
    private var totalHarga: Int = 0
    private var subtotalTambahan: Int = 0
    private var metodePembayaran: String = ""
    private var namaKaryawan: String = ""
    private var tambahanList: ArrayList<model_tambahan> = ArrayList()
    private var noTransaksi: String = ""
    private var tanggalTransaksi: String = ""
    private var hargaLayananString: String = ""

    // Flag untuk mencegah duplikasi simpan data
    private var isDataSaved = false

    // Bluetooth variables
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    // Coroutine scope for background tasks
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSION = 1001
        private const val REQUEST_ENABLE_BT = 1002
        private const val PRINTER_MAC_ADDRESS = "DC:0D:51:A7:FF:7A" // Ganti dengan MAC address printer Anda
        private val PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val TAG = "Invoice"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_invoice)

        setupWindowInsets()
        initViews()
        extractIntentData()
        setupInvoiceData()
        setupClickListeners()

        // Simpan data laporan sekali saat activity dibuat
        saveTransactionData()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        // Header
        tvBusinessName = findViewById(R.id.tv_business_name)
        tvBranch = findViewById(R.id.tv_branch)

        // Transaction Details
        tvTransactionId = findViewById(R.id.tv_transaction_id)
        tvDate = findViewById(R.id.tv_date)
        tvCustomer = findViewById(R.id.tv_customer)
        tvCustomerPhone = findViewById(R.id.tv_customer_phone)
        tvEmployee = findViewById(R.id.tv_employee)
        tvPaymentMethod = findViewById(R.id.tv_payment_method)

        // Main Service
        tvMainService = findViewById(R.id.tv_main_service)
        tvMainServiceQuantity = findViewById(R.id.tv_main_service_quantity)
        tvMainServicePricePerUnit = findViewById(R.id.tv_main_service_price_per_unit)
        tvMainServicePrice = findViewById(R.id.tv_main_service_price)

        // Additional Services
        tvAdditionalServicesHeader = findViewById(R.id.tv_additional_services_header)
        rvAdditionalServices = findViewById(R.id.rv_additional_services)
        tvSubtotalAdditional = findViewById(R.id.tv_subtotal_additional)

        // Total
        tvTotal = findViewById(R.id.tv_total)

        // Buttons
        btnWhatsapp = findViewById(R.id.btn_whatsapp)
        btnPrint = findViewById(R.id.btn_print)
    }

    private fun extractIntentData() {
        Log.d(TAG, "Starting extractIntentData")

        // Standardisasi pengambilan data dengan prioritas key yang konsisten
        namaPelanggan = intent.getStringExtra("namaPelanggan")
            ?: intent.getStringExtra("pelanggan")
                    ?: intent.getStringExtra("nama_pelanggan") ?: ""

        nomorHp = intent.getStringExtra("noHP")
            ?: intent.getStringExtra("nomor_hp")
                    ?: intent.getStringExtra("nomorHp") ?: ""

        namaLayanan = intent.getStringExtra("namaLayanan")
            ?: intent.getStringExtra("layanan")
                    ?: intent.getStringExtra("nama_layanan") ?: ""

        // Handle harga layanan dengan prioritas dari Int
        hargaLayanan = intent.getIntExtra("hargaLayananInt", 0)
        if (hargaLayanan == 0) {
            hargaLayanan = intent.getIntExtra("hargaLayanan", 0)
        }

        hargaLayananString = intent.getStringExtra("hargaLayananString")
            ?: intent.getStringExtra("hargaLayanan")
                    ?: formatCurrency(hargaLayanan)

        jumlahLayanan = intent.getIntExtra("jumlahLayanan", 1)
        totalHargaLayanan = intent.getIntExtra("totalHargaLayanan", hargaLayanan * jumlahLayanan)
        totalHarga = intent.getIntExtra("totalBayar", 0)
        subtotalTambahan = intent.getIntExtra("subtotalTambahan", 0)

        metodePembayaran = intent.getStringExtra("metodePembayaran")
            ?: intent.getStringExtra("metode_pembayaran") ?: ""

        namaKaryawan = intent.getStringExtra("karyawan")
            ?: intent.getStringExtra("nama_karyawan") ?: "Admin"

        // Handle layanan tambahan
        val serializableExtra = intent.getSerializableExtra("layananTambahan")
            ?: intent.getSerializableExtra("tambahan")

        tambahanList = try {
            @Suppress("UNCHECKED_CAST")
            serializableExtra as? ArrayList<model_tambahan> ?: ArrayList()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing tambahan list: ${e.message}")
            ArrayList()
        }

        // Generate transaction data
        noTransaksi = intent.getStringExtra("idTransaksi")
            ?: intent.getStringExtra("no_transaksi")
                    ?: generateNoTransaksi()

        tanggalTransaksi = intent.getStringExtra("tanggal")
            ?: intent.getStringExtra("date")
                    ?: getCurrentDateTime()

        // Recalculate values if needed
        if (subtotalTambahan == 0 && tambahanList.isNotEmpty()) {
            subtotalTambahan = tambahanList.sumOf {
                val hargaStr = it.hargaTambahan ?: "0"
                hargaStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
            }
        }

        if (totalHarga == 0) {
            totalHarga = totalHargaLayanan + subtotalTambahan
        }

        Log.d(TAG, "=== EXTRACTED DATA ===")
        Log.d(TAG, "noTransaksi: $noTransaksi")
        Log.d(TAG, "namaPelanggan: $namaPelanggan")
        Log.d(TAG, "namaLayanan: $namaLayanan")
        Log.d(TAG, "totalHarga: $totalHarga")
        Log.d(TAG, "metodePembayaran: $metodePembayaran")
        Log.d(TAG, "====================")
    }

    private fun setupInvoiceData() {
        // Header info
        tvBusinessName.text = "SAMM Laundry"
        tvBranch.text = "Solo"

        // Transaction details
        tvTransactionId.text = noTransaksi
        tvDate.text = tanggalTransaksi
        tvCustomer.text = if (namaPelanggan.isNotEmpty()) namaPelanggan else "Pelanggan"
        tvCustomerPhone.text = if (nomorHp.isNotEmpty()) nomorHp else "-"
        tvEmployee.text = namaKaryawan
        tvPaymentMethod.text = if (metodePembayaran.isNotEmpty()) metodePembayaran else "Belum Dipilih"

        // Main service
        tvMainService.text = if (namaLayanan.isNotEmpty()) namaLayanan else "Layanan"
        tvMainServiceQuantity.text = "Jumlah: $jumlahLayanan"

        if (jumlahLayanan > 1) {
            tvMainServicePricePerUnit.text = "@ ${formatCurrency(hargaLayanan)}"
            tvMainServicePrice.text = formatCurrency(totalHargaLayanan)
        } else {
            tvMainServicePricePerUnit.text = "@ ${formatCurrency(hargaLayanan)}"
            tvMainServicePrice.text = formatCurrency(hargaLayanan)
        }

        // Additional services
        setupAdditionalServices()

        // Total
        tvTotal.text = formatCurrency(totalHarga)

        Log.d(TAG, "Invoice UI setup completed")
    }

    private fun setupAdditionalServices() {
        if (tambahanList.isEmpty()) {
            hideAdditionalServices()
            tvSubtotalAdditional.text = formatCurrency(0)
            return
        }

        tvAdditionalServicesHeader.visibility = View.VISIBLE
        rvAdditionalServices.layoutManager = LinearLayoutManager(this)
        val adapter = AdditionalServicesAdapter(tambahanList)
        rvAdditionalServices.adapter = adapter
        rvAdditionalServices.visibility = View.VISIBLE
        tvSubtotalAdditional.text = formatCurrency(subtotalTambahan)
    }

    private fun hideAdditionalServices() {
        tvAdditionalServicesHeader.visibility = View.GONE
        rvAdditionalServices.visibility = View.GONE
    }

    private fun setupClickListeners() {
        btnWhatsapp.setOnClickListener {
            shareToWhatsApp()
            goToDataLaporan()
        }

        btnPrint.setOnClickListener {
            cetakStruk()
        }
    }

    // ==================== LAPORAN MANAGEMENT - FIXED ====================

    private fun saveTransactionData() {
        if (isDataSaved) {
            Log.d(TAG, "Data already saved, skipping...")
            return
        }

        Log.d(TAG, "Starting saveTransactionData")

        // Validasi data sebelum simpan
        if (!validateTransactionData()) {
            Log.e(TAG, "Data validation failed")
            showToast("Data transaksi tidak lengkap!")
            return
        }

        // Format tanggal untuk laporan (YYYY-MM-DD)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = sdf.format(Date())

        // Tentukan status berdasarkan metode pembayaran
        val status = determinePaymentStatus(metodePembayaran)

        val newLaporan = model_laporan().apply {
            noTransaksi = this@Invoice.noTransaksi
            tanggal = formattedDate
            namaPelanggan = this@Invoice.namaPelanggan
            namaLayanan = this@Invoice.namaLayanan
            totalHarga = this@Invoice.totalHarga
            this.status = status
        }

        Log.d(TAG, "Saving laporan: noTransaksi=${newLaporan.noTransaksi}, nama=${newLaporan.namaPelanggan}, total=${newLaporan.totalHarga}, status=${newLaporan.status}")

        // Simpan ke Firebase
        val database = FirebaseDatabase.getInstance().getReference("Laporan")
        database.child(noTransaksi).setValue(newLaporan)
            .addOnSuccessListener {
                isDataSaved = true
                Log.d(TAG, "✅ Data berhasil disimpan ke Firebase dengan ID: $noTransaksi")
                showToast("Data transaksi berhasil disimpan!")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Gagal menyimpan data: ${exception.message}")
                showToast("Gagal menyimpan data transaksi: ${exception.message}")
                exception.printStackTrace()
            }
    }

    private fun determinePaymentStatus(metodePembayaran: String): StatusLaporan {
        return when (metodePembayaran.lowercase()) {
            "tunai", "cash", "dana", "gopay", "ovo", "qris", "shopeepay", "transfer bank" -> {
                Log.d(TAG, "Status: SUDAH_DIBAYAR untuk metode: $metodePembayaran")
                StatusLaporan.SUDAH_DIBAYAR
            }
            "bayar nanti", "nanti" -> {
                Log.d(TAG, "Status: BELUM_DIBAYAR untuk metode: $metodePembayaran")
                StatusLaporan.BELUM_DIBAYAR
            }
            else -> {
                Log.d(TAG, "Status: BELUM_DIBAYAR (default) untuk metode: $metodePembayaran")
                StatusLaporan.BELUM_DIBAYAR
            }
        }
    }


    private fun formatCurrency(amount: Int): String {
        return "Rp${String.format("%,d", amount).replace(',', '.')}"
    }

    private fun shareToWhatsApp() {
        val message = createWhatsAppMessage()
        val phoneNumber = formatPhoneNumber(nomorHp)
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val url = "https://wa.me/$phoneNumber?text=$encodedMessage"

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp")
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
            showToast("Pesan WhatsApp berhasil dikirim!")
        } catch (e: Exception) {
            showToast("Gagal membuka WhatsApp: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createWhatsAppMessage(): String {
        val tambahanText = if (tambahanList.isNotEmpty()) {
            tambahanList.joinToString("\n") {
                val harga = it.hargaTambahan?.replace("[^0-9]".toRegex(), "")?.toIntOrNull() ?: 0
                "- ${it.namaTambahan}: ${formatCurrency(harga)}"
            }
        } else {
            "Tidak ada layanan tambahan"
        }

        val layananText = if (jumlahLayanan > 1) {
            "$namaLayanan (${jumlahLayanan}x ${formatCurrency(hargaLayanan)}) = ${formatCurrency(totalHargaLayanan)}"
        } else {
            "$namaLayanan - ${formatCurrency(hargaLayanan)}"
        }

        return """
════════════════════════════
        MAHSOK LAUNDRY         
           SOLO
════════════════════════════

HALO, $namaPelanggan 👋

DETAIL TRANSAKSI:
  ID Transaksi   : $noTransaksi
  Tanggal        : $tanggalTransaksi

INFORMASI PELANGGAN:
  Nama           : $namaPelanggan
  No. HP         : $nomorHp

LAYANAN:
  Utama          : $layananText

LAYANAN TAMBAHAN:
$tambahanText

PEMBAYARAN:
  Metode         : $metodePembayaran
  Total Bayar    : ${formatCurrency(totalHarga)}

════════════════════════════
🙏 Terima kasih telah mempercayakan 
cucian Anda kepada kami!
Kami akan memberikan pelayanan 
terbaik untuk Anda!

📍 Mahsok Laundry - Cabang Solo
════════════════════════════
        """.trimIndent()
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        return when {
            cleanNumber.startsWith("0") -> "62${cleanNumber.substring(1)}"
            cleanNumber.startsWith("62") -> cleanNumber
            else -> "62$cleanNumber"
        }
    }

    private fun generateNoTransaksi(): String {
        val timestamp = System.currentTimeMillis()
        return "TRX${timestamp.toString().takeLast(8)}"
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun goToDataLaporan() {
        Log.d(TAG, "Navigating to data_laporan")
        try {
            val intent = Intent(this, data_laporan::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to data_laporan: ${e.message}")
            showToast("Gagal membuka halaman laporan: ${e.message}")
        }
    }

    // ==================== PRINTING FUNCTIONS ====================

    private fun cetakStruk() {
        val view = findViewById<LinearLayout>(R.id.invoice_activity)
        saveToPdf(view)

        if (checkBluetoothPermissions()) {
            printViaBluetooth()
        } else {
            requestBluetoothPermissions()
        }
    }

    private fun saveToPdf(view: LinearLayout) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = document.startPage(pageInfo)

        view.draw(page.canvas)
        document.finishPage(page)

        val directory = File(getExternalFilesDir(null), "struk")
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, "Struk_Mahsok_Laundry_${noTransaksi}_${System.currentTimeMillis()}.pdf")

        try {
            document.writeTo(FileOutputStream(file))
            showToast("Struk disimpan: ${file.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
            showToast("Gagal menyimpan struk")
        }

        document.close()
    }

    private fun printViaBluetooth() {
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions()
            return
        }

        thread {
            try {
                connectToPrinter(PRINTER_MAC_ADDRESS)
                printTextReceipt()

                Handler(Looper.getMainLooper()).post {
                    showToast("Struk berhasil dicetak via Bluetooth")
                    goToDataLaporan()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    showToast("Gagal mencetak: ${e.message}")
                    goToDataLaporan()
                }
            } finally {
                disconnectPrinter()
            }
        }
    }

    private fun connectToPrinter(macAddress: String) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            throw Exception("Bluetooth tidak tersedia atau tidak aktif")
        }

        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            throw Exception("Izin Bluetooth diperlukan")
        }

        bluetoothSocket = device.createRfcommSocketToServiceRecord(PRINTER_UUID)
        bluetoothSocket?.connect()
        outputStream = bluetoothSocket?.outputStream
    }

    private fun disconnectPrinter() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun printTextReceipt() {
        val layananText = if (jumlahLayanan > 1) {
            "$namaLayanan (${jumlahLayanan}x) - ${formatCurrency(totalHargaLayanan)}"
        } else {
            "$namaLayanan - ${formatCurrency(hargaLayanan)}"
        }

        val tambahanText = if (tambahanList.isNotEmpty()) {
            tambahanList.joinToString("\n") { item ->
                val harga = item.hargaTambahan?.replace("[^0-9]".toRegex(), "")?.toIntOrNull() ?: 0
                "  ${item.namaTambahan} -  ${formatCurrency(harga)}"
            }
        } else {
            "  Tidak ada layanan tambahan"
        }

        val receiptText = """
[C]===MAHSOK LAUNDRY===
[C]SOLO
[L]ID: $noTransaksi
[L]TGL: $tanggalTransaksi
[L]NAMA: $namaPelanggan
[L]HP: $nomorHp
[L]KARYAWAN: $namaKaryawan
[L]----------- LAYANAN -----------
[L]$layananText
[L]-------------------------------
${if (tambahanList.isNotEmpty()) "[L]Tambahan:\n$tambahanText" else ""}
[L]-------------------------------
[L]Subt. Tambahan: ${formatCurrency(subtotalTambahan)}
[L]TOTAL: ${formatCurrency(totalHarga)}
[L]Bayar: $metodePembayaran
[C]========== TERIMA KASIH ==========
[C]MAHSOK LAUNDRY - SOLO

        """.trimIndent()

        // Initialize printer
        outputStream?.write(byteArrayOf(0x1B, 0x40)) // ESC @

        // Process and print text
        val lines = receiptText.split("\n")
        for (line in lines) {
            when {
                line.startsWith("[C]") -> {
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 1)) // Center align
                    val text = line.substring(3)
                    if (text.isNotEmpty()) outputStream?.write(text.toByteArray(Charsets.UTF_8))
                }
                line.startsWith("[L]") -> {
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 0)) // Left align
                    val text = line.substring(3)
                    outputStream?.write(text.toByteArray(Charsets.UTF_8))
                }
                else -> {
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 0)) // Left align
                    if (line.isNotEmpty()) outputStream?.write(line.toByteArray(Charsets.UTF_8))
                }
            }
            outputStream?.write(byteArrayOf(0x0A)) // Line feed
        }

        // Feed paper and cut
        outputStream?.write(byteArrayOf(0x1B, 0x64, 3)) // Feed 3 lines
        outputStream?.write(byteArrayOf(0x1D, 0x56, 0x00)) // Cut paper
    }

    private fun checkBluetoothPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            val bluetoothPermission = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED

            val bluetoothAdminPermission = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED

            bluetoothPermission && bluetoothAdminPermission
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        ActivityCompat.requestPermissions(this, permissions, REQUEST_BLUETOOTH_PERMISSION)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(TAG, "Bluetooth permissions granted")
                    showToast("Izin Bluetooth diberikan")
                    printViaBluetooth()
                } else {
                    Log.w(TAG, "Bluetooth permissions denied")
                    showToast("Izin Bluetooth diperlukan untuk mencetak")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Bluetooth enabled by user")
                    showToast("Bluetooth diaktifkan")
                    printViaBluetooth()
                } else {
                    Log.w(TAG, "Bluetooth enable request denied")
                    showToast("Bluetooth diperlukan untuk mencetak")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup resources
        disconnectPrinter()
        coroutineScope.cancel()
        Log.d(TAG, "Invoice activity destroyed and resources cleaned up")
    }

    // ==================== ADDITIONAL SERVICES ADAPTER ====================

    inner class AdditionalServicesAdapter(
        private val additionalServices: List<model_tambahan>
    ) : RecyclerView.Adapter<AdditionalServicesAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvServiceName: TextView = view.findViewById(R.id.tv_service_name)
            val tvServicePrice: TextView = view.findViewById(R.id.tv_service_price)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_additional_service, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val service = additionalServices[position]

            holder.tvServiceName.text = service.namaTambahan ?: "Layanan Tambahan"

            val harga = service.hargaTambahan?.replace("[^0-9]".toRegex(), "")?.toIntOrNull() ?: 0
            holder.tvServicePrice.text = formatCurrency(harga)
        }

        override fun getItemCount(): Int = additionalServices.size
    }

    // ==================== UTILITY FUNCTIONS ====================

    private fun validateTransactionData(): Boolean {
        return !(noTransaksi.isEmpty() ||
                namaPelanggan.isEmpty() ||
                namaLayanan.isEmpty() ||
                totalHarga <= 0)
    }

    private fun logTransactionSummary() {
        Log.d(TAG, "=== TRANSACTION SUMMARY ===")
        Log.d(TAG, "Transaction ID: $noTransaksi")
        Log.d(TAG, "Customer: $namaPelanggan")
        Log.d(TAG, "Service: $namaLayanan")
        Log.d(TAG, "Main Service Total: ${formatCurrency(totalHargaLayanan)}")
        Log.d(TAG, "Additional Services Total: ${formatCurrency(subtotalTambahan)}")
        Log.d(TAG, "Grand Total: ${formatCurrency(totalHarga)}")
        Log.d(TAG, "Payment Method: $metodePembayaran")
        Log.d(TAG, "Employee: $namaKaryawan")
        Log.d(TAG, "========================")
    }

    // ==================== ERROR HANDLING ====================

    private fun handleError(error: Exception, operation: String) {
        Log.e(TAG, "Error during $operation: ${error.message}")
        error.printStackTrace()
        showToast("Terjadi kesalahan: ${error.message}")
    }

}