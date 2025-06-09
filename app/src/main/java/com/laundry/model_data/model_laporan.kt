package com.laundry.model_data

data class model_laporan(
    var noTransaksi: String? = null,
    var tanggal: String? = null,
    var namaPelanggan: String? = null,
    var namaLayanan: String? = null,
    var totalHarga: Int = 0,
    var status: StatusLaporan = StatusLaporan.BELUM_DIBAYAR,

    // Data tambahan untuk informasi lebih lengkap
    var jumlahLayanan: Int = 1,
    var hargaLayanan: Int = 0,
    var totalHargaLayanan: Int = 0,
    var jumlahLayananTambahan: Int = 0,
    var subtotalTambahan: Int = 0,
    var layananTambahan: ArrayList<model_tambahan>? = null,
    var metodePembayaran: String? = null,
    var namaKaryawan: String? = null,
    var nomorHp: String? = null
)