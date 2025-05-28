package com.laundry.invoice

import android.Manifest
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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.laundry.R
import com.laundry.model_data.model_tambahan
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.concurrent.thread

class Invoice : AppCompatActivity() {

    companion object {
        private val PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val REQUEST_BLUETOOTH_PERMISSION = 1001

        // Ganti dengan MAC address printer Bluetooth Anda
        private const val PRINTER_MAC_ADDRESS = "DC:0D:51:A7:FF:7A"
    }

    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        val idTransaksi = intent.getStringExtra("idTransaksi") ?: "-"
        val tanggal = intent.getStringExtra("tanggal") ?: "-"
        val pelanggan = intent.getStringExtra("pelanggan") ?: "-"
        val noHP = intent.getStringExtra("noHP") ?: "-"
        val karyawan = intent.getStringExtra("karyawan") ?: "-"
        val layanan = intent.getStringExtra("layanan") ?: "-"
        val hargaLayanan = intent.getIntExtra("hargaLayanan", 0)
        val metodePembayaran = intent.getStringExtra("metodePembayaran") ?: "-"
        val tambahan = intent.getSerializableExtra("tambahan") as? List<model_tambahan> ?: emptyList()

        // Set text untuk informasi transaksi
        findViewById<TextView>(R.id.tvIdTransaksi).text = "ID Transaksi: $idTransaksi"
        findViewById<TextView>(R.id.tvTanggal).text = "Tanggal: $tanggal"
        findViewById<TextView>(R.id.tvPelanggan).text = "Pelanggan: $pelanggan"
        findViewById<TextView>(R.id.tvNoHP).text = "No. HP: $noHP"
        findViewById<TextView>(R.id.tvKaryawan).text = "Karyawan: $karyawan"
        findViewById<TextView>(R.id.tvMetodePembayaran).text = "Metode: $metodePembayaran"
        findViewById<TextView>(R.id.tvLayananUtama).text = layanan
        findViewById<TextView>(R.id.tvHargaUtama).text = formatRupiah(hargaLayanan)

        // Setup layanan tambahan
        setupLayananTambahan(tambahan, hargaLayanan)

        // Setup button listeners
        findViewById<Button>(R.id.btnWhatsapp).setOnClickListener {
            kirimKeWhatsApp()
        }

        findViewById<Button>(R.id.btnCetak).setOnClickListener {
            cetakStruk()
        }
    }

    private fun setupLayananTambahan(tambahan: List<model_tambahan>, hargaLayanan: Int) {
        val container = findViewById<LinearLayout>(R.id.containerTambahan)
        var subtotal = 0

        // Clear container terlebih dahulu
        container.removeAllViews()

        if (tambahan.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = "Tidak ada layanan tambahan"
            emptyView.textSize = 12f
            emptyView.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            container.addView(emptyView)
        } else {
            tambahan.forEachIndexed { index, item ->
                val itemView = createTambahanItemView(index + 1, item)
                container.addView(itemView)
                subtotal += item.hargaTambahan.toIntOrNull() ?: 0
            }
        }

        findViewById<TextView>(R.id.tvSubtotalTambahan).text = "Subtotal Tambahan: ${formatRupiah(subtotal)}"
        findViewById<TextView>(R.id.tvTotalBayar).text = "Total Bayar: ${formatRupiah(subtotal + hargaLayanan)}"
    }

    private fun createTambahanItemView(nomor: Int, item: model_tambahan): TextView {
        val itemView = TextView(this)
        val harga = item.hargaTambahan.toIntOrNull() ?: 0

        itemView.text = "[$nomor] ${item.namaTambahan}\n${formatRupiah(harga)}"
        itemView.textSize = 14f
        itemView.setTextColor(resources.getColor(android.R.color.black, null))
        itemView.setPadding(0, 8, 0, 8)

        return itemView
    }

    private fun formatRupiah(amount: Int): String {
        return "Rp${String.format("%,d", amount).replace(',', '.')}"
    }

    private fun kirimKeWhatsApp() {
        val noHP = intent.getStringExtra("noHP") ?: return
        val idTransaksi = intent.getStringExtra("idTransaksi") ?: "-"
        val tanggal = intent.getStringExtra("tanggal") ?: "-"
        val pelanggan = intent.getStringExtra("pelanggan") ?: "-"
        val layanan = intent.getStringExtra("layanan") ?: "-"
        val hargaLayanan = formatRupiah(intent.getIntExtra("hargaLayanan", 0))
        val metode = intent.getStringExtra("metodePembayaran") ?: "-"
        val tambahan = intent.getSerializableExtra("tambahan") as? List<model_tambahan> ?: emptyList()

        val tambahanText = if (tambahan.isNotEmpty()) {
            tambahan.joinToString("\n") { "- ${it.namaTambahan}: ${formatRupiah(it.hargaTambahan.toIntOrNull() ?: 0)}" }
        } else {
            "Tidak ada layanan tambahan"
        }

        val total = findViewById<TextView>(R.id.tvTotalBayar).text.toString()

        val message = """
            ════════════════════════════
         LAUNDRY - SAMM         
════════════════════════════

HALO, $pelanggan

DETAIL TRANSAKSI:
  ID Transaksi   : $idTransaksi
  Tanggal        : $tanggal

INFORMASI PELANGGAN:
  Nama           : $pelanggan
  No. HP         : $noHP

LAYANAN:
  Utama          : $layanan
  Harga          : $hargaLayanan

LAYANAN TAMBAHAN:
$tambahanText

PEMBAYARAN:
  Metode         : $metode
  Total Bayar    : $total

════════════════════════════
Terima kasih telah menggunakan
layanan Laundry - SAMM!
════════════════════════════

        """.trimIndent()

        // Format nomor dengan benar (hapus karakter non-digit dan tambahkan 62)
        val cleanNumber = noHP.replace(Regex("[^0-9]"), "") // Hapus semua karakter non-digit
        val whatsappNumber = if (cleanNumber.startsWith("0")) {
            "62${cleanNumber.substring(1)}" // Ganti 0 di awal dengan 62
        } else if (cleanNumber.startsWith("62")) {
            cleanNumber // Sudah format internasional
        } else {
            "62$cleanNumber" // Tambahkan 62 di depan
        }

        // Encode pesan untuk URL
        val encodedMessage = Uri.encode(message)
        val url = "https://wa.me/$whatsappNumber?text=$encodedMessage"

        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            setPackage("com.whatsapp") // Pastikan membuka WhatsApp langsung
        }

        try {
            // Cek apakah WhatsApp terinstall
            if (whatsappIntent.resolveActivity(packageManager) != null) {
                startActivity(whatsappIntent)
            } else {
                // Jika WhatsApp tidak terinstall, buka di browser
                val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                }
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cetakStruk() {
        val view = findViewById<LinearLayout>(R.id.invoice_activity)

        // Simpan ke PDF (existing functionality)
        saveToPdf(view)

        // Print via Bluetooth
        printViaBluetooth(view)
    }

    private fun saveToPdf(view: LinearLayout) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = document.startPage(pageInfo)

        view.draw(page.canvas)
        document.finishPage(page)

        val directory = File(getExternalFilesDir(null), "struk")
        if (!directory.exists()) directory.mkdirs()

        val idTransaksi = intent.getStringExtra("idTransaksi") ?: "Unknown"
        val file = File(directory, "Struk_Laundry_SAMM_${idTransaksi}_${System.currentTimeMillis()}.pdf")

        try {
            document.writeTo(FileOutputStream(file))
            Toast.makeText(this, "Struk disimpan: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan struk", Toast.LENGTH_SHORT).show()
        }

        document.close()
    }

    private fun printViaBluetooth(view: LinearLayout) {
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions()
            return
        }

        thread {
            try {
                connectToPrinter(PRINTER_MAC_ADDRESS)

                // Pilih metode print: bitmap atau text
                // printBitmapReceipt(view) // Print sebagai gambar
                printTextReceipt() // Print sebagai text (lebih cepat)

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this@Invoice, "Struk berhasil dicetak via Bluetooth", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this@Invoice, "Gagal mencetak: ${e.message}", Toast.LENGTH_SHORT).show()
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
        val idTransaksi = intent.getStringExtra("idTransaksi") ?: "-"
        val tanggal = intent.getStringExtra("tanggal") ?: "-"
        val pelanggan = intent.getStringExtra("pelanggan") ?: "-"
        val noHP = intent.getStringExtra("noHP") ?: "-"
        val karyawan = intent.getStringExtra("karyawan") ?: "-"
        val layanan = intent.getStringExtra("layanan") ?: "-"
        val hargaLayanan = intent.getIntExtra("hargaLayanan", 0)
        val metodePembayaran = intent.getStringExtra("metodePembayaran") ?: "-"
        val tambahan = intent.getSerializableExtra("tambahan") as? List<model_tambahan> ?: emptyList()

        var subtotalTambahan = 0
        val tambahanText = if (tambahan.isNotEmpty()) {
            tambahan.joinToString("\n") { item ->
                val harga = item.hargaTambahan.toIntOrNull() ?: 0
                subtotalTambahan += harga
                "  ${item.namaTambahan} -  ${formatRupiah(harga)}"
            }
        } else {
            "  Tidak ada layanan tambahan"
        }

        val total = subtotalTambahan + hargaLayanan

        val receiptText = """
[C]===LAUNDRY - SAMM===
[L]ID: $idTransaksi
[L]TGL: $tanggal
[L]NAMA: $pelanggan
[L]HP: $noHP
[L]KARYAWAN: $karyawan
[L]----------- LAYANAN -----------
[L]$layanan - ${formatRupiah(hargaLayanan)}
[L]-------------------------------
${if (tambahanText.isNotEmpty()) "[L]Tambahan:\n$tambahanText" else ""}
[L]-------------------------------
[L]Subt. Tambahan: ${formatRupiah(subtotalTambahan)}
[L]TOTAL: ${formatRupiah(total)}
[L]Bayar: $metodePembayaran
[C]========== TERIMA KASIH ==========
[C]LAUNDRY - SAMM

        """.trimIndent()

        // Initialize printer
        outputStream?.write(byteArrayOf(0x1B, 0x40)) // ESC @

        // Process and print text
        val lines = receiptText.split("\n")
        for (line in lines) {
            when {
                line.startsWith("[C]") -> {
                    // Center align
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 1)) // ESC a 1
                    val text = line.substring(3)
                    if (text.isNotEmpty()) outputStream?.write(text.toByteArray(Charsets.UTF_8))
                }
                line.startsWith("[R]") -> {
                    // Right align
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 2)) // ESC a 2
                    val text = line.substring(3)
                    if (text.contains("[B]")) {
                        // Bold text
                        outputStream?.write(byteArrayOf(0x1B, 0x45, 1)) // ESC E 1 (bold on)
                        outputStream?.write(text.replace("[B]", "").toByteArray(Charsets.UTF_8))
                        outputStream?.write(byteArrayOf(0x1B, 0x45, 0)) // ESC E 0 (bold off)
                    } else {
                        outputStream?.write(text.toByteArray(Charsets.UTF_8))
                    }
                }
                line.startsWith("[L]") -> {
                    // Left align
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 0)) // ESC a 0
                    val text = line.substring(3)
                    if (text.contains("[B]")) {
                        // Bold text
                        outputStream?.write(byteArrayOf(0x1B, 0x45, 1)) // ESC E 1 (bold on)
                        outputStream?.write(text.replace("[B]", "").toByteArray(Charsets.UTF_8))
                        outputStream?.write(byteArrayOf(0x1B, 0x45, 0)) // ESC E 0 (bold off)
                    } else {
                        outputStream?.write(text.toByteArray(Charsets.UTF_8))
                    }
                }
                else -> {
                    // Normal text
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 0)) // Left align
                    if (line.isNotEmpty()) outputStream?.write(line.toByteArray(Charsets.UTF_8))
                }
            }
            outputStream?.write(byteArrayOf(0x0A)) // Line feed
        }

        // Feed paper and cut
        outputStream?.write(byteArrayOf(0x1B, 0x64, 3)) // Feed 3 lines
        outputStream?.write(byteArrayOf(0x1D, 0x56, 0x00)) // Cut paper (partial)
    }

    private fun printBitmapReceipt(view: LinearLayout) {
        // Convert view to bitmap
        val bitmap = createBitmapFromView(view)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 384,
            (bitmap.height * 384) / bitmap.width, false)

        val pixels = IntArray(resizedBitmap.width * resizedBitmap.height)
        resizedBitmap.getPixels(pixels, 0, resizedBitmap.width, 0, 0,
            resizedBitmap.width, resizedBitmap.height)

        outputStream?.write(byteArrayOf(0x1B, 0x40)) // Initialize printer

        val width = resizedBitmap.width
        val height = resizedBitmap.height

        for (y in 0 until height step 24) {
            outputStream?.write(byteArrayOf(0x1B, 0x2A, 33, (width and 0xFF).toByte(),
                ((width shr 8) and 0xFF).toByte()))

            for (x in 0 until width) {
                val bytes = ByteArray(3)
                for (k in 0..2) {
                    var slice = 0
                    for (b in 0..7) {
                        val yPos = y + k * 8 + b
                        if (yPos < height) {
                            val pixel = pixels[yPos * width + x]
                            val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                            if (gray < 128) {
                                slice = slice or (1 shl (7 - b))
                            }
                        }
                    }
                    bytes[k] = slice.toByte()
                }
                outputStream?.write(bytes)
            }
            outputStream?.write(byteArrayOf(0x0A)) // Line feed
        }

        // Feed paper
        outputStream?.write(byteArrayOf(0x1B, 0x64, 3)) // Feed 3 lines
    }

    private fun createBitmapFromView(view: LinearLayout): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE) // Background putih
        view.draw(canvas)
        return bitmap
    }

    private fun checkBluetoothPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        ActivityCompat.requestPermissions(this, permissions, REQUEST_BLUETOOTH_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, retry printing
                cetakStruk()
            } else {
                Toast.makeText(this, "Izin Bluetooth diperlukan untuk mencetak", Toast.LENGTH_SHORT).show()
            }
        }
    }
}