package com.laundry

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class edit_account : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    // UI Components
    private lateinit var backButton: ImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var editProfileImageButton: ImageView
    private lateinit var etUserName: EditText
    private lateinit var etUserPhone: EditText
    private lateinit var etUserEmail: EditText
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    // Image handling
    private var selectedImageUri: Uri? = null
    private var isImageChanged = false

    companion object {
        private const val TAG = "EditAccountActivity"
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    // Activity result launcher for image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                isImageChanged = true
                Log.d(TAG, "Image selected: $uri")

                // Load selected image into ImageView
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(profileImage)

                Log.d(TAG, "Image loaded into ImageView")
            } ?: run {
                Log.e(TAG, "No image data received")
            }
        } else {
            Log.d(TAG, "Image picker cancelled or failed. Result code: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_account)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth and Storage
        auth = Firebase.auth
        storage = Firebase.storage

        // Initialize UI components
        initViews()

        // Load current user data
        loadCurrentUserData()

        // Set click listeners
        setupClickListeners()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profileImage)
        editProfileImageButton = findViewById(R.id.editProfileImageButton)
        etUserName = findViewById(R.id.etUserName)
        etUserPhone = findViewById(R.id.etUserPhone)
        etUserEmail = findViewById(R.id.etUserEmail)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
    }

    private fun loadCurrentUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Set current user name
            val displayName = currentUser.displayName
            if (!displayName.isNullOrEmpty()) {
                etUserName.setText(displayName)
            }

            // Set current user email
            val email = currentUser.email
            if (!email.isNullOrEmpty()) {
                etUserEmail.setText(email)

                // Extract and set phone number if email format is phone@laundryapp.com
                if (email.endsWith("@laundryapp.com")) {
                    val phone = email.substringBefore("@laundryapp.com")
                    if (phone.matches(Regex("^08[0-9]{8,11}$"))) {
                        etUserPhone.setText(phone)
                    }
                }
            }

            // Load profile image if available
            val photoUrl = currentUser.photoUrl
            if (photoUrl != null) {
                Log.d(TAG, "Loading profile photo: $photoUrl")
                // Use Glide to load profile image
                Glide.with(this)
                    .load(photoUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(profileImage)
            } else {
                Log.d(TAG, "No profile photo available")
                // Set default image
                profileImage.setImageResource(R.drawable.ic_person_placeholder)
            }
        } else {
            // User not logged in, redirect to login
            navigateToLogin()
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        editProfileImageButton.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun checkPermissionAndOpenGallery() {
        // For Android 13+ (API level 33+), use READ_MEDIA_IMAGES
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"

            // Add MIME types untuk memastikan hanya gambar yang bisa dipilih
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/jpg"))

            Log.d(TAG, "Opening image picker")
            imagePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening image picker", e)
            Toast.makeText(this, getString(R.string.error_opening_gallery), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    Toast.makeText(this, getString(R.string.permission_required_gallery), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveUserData() {
        val newName = etUserName.text.toString().trim()
        val newPhone = etUserPhone.text.toString().trim()
        val newEmail = etUserEmail.text.toString().trim()
        val currentPassword = etCurrentPassword.text.toString()
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Validate input
        if (newName.isEmpty()) {
            etUserName.error = "Nama tidak boleh kosong"
            etUserName.requestFocus()
            return
        }

        if (newPhone.isNotEmpty() && !newPhone.matches(Regex("^08[0-9]{8,11}$"))) {
            etUserPhone.error = "Format nomor telepon tidak valid (08xxxxxxxxxx)"
            etUserPhone.requestFocus()
            return
        }

        if (newEmail.isEmpty()) {
            etUserEmail.error = "Email tidak boleh kosong"
            etUserEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            etUserEmail.error = "Format email tidak valid"
            etUserEmail.requestFocus()
            return
        }

        // Validate password fields if any password field is filled
        if (currentPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (currentPassword.isEmpty()) {
                etCurrentPassword.error = "Password saat ini wajib diisi"
                etCurrentPassword.requestFocus()
                return
            }

            if (newPassword.isEmpty()) {
                etNewPassword.error = "Password baru wajib diisi"
                etNewPassword.requestFocus()
                return
            }

            if (newPassword.length < 6) {
                etNewPassword.error = "Password minimal 6 karakter"
                etNewPassword.requestFocus()
                return
            }

            if (newPassword != confirmPassword) {
                etConfirmPassword.error = "Konfirmasi password tidak cocok"
                etConfirmPassword.requestFocus()
                return
            }
        }

        // Show loading state
        saveButton.isEnabled = false
        saveButton.text = "Menyimpan..."

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Check if password change is requested
            if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                // Re-authenticate user before changing password
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                currentUser.reauthenticate(credential)
                    .addOnCompleteListener { reAuthTask ->
                        if (reAuthTask.isSuccessful) {
                            // Change password
                            currentUser.updatePassword(newPassword)
                                .addOnCompleteListener { passwordTask ->
                                    if (passwordTask.isSuccessful) {
                                        // Continue with other updates
                                        updateProfileData(currentUser, newName, newEmail)
                                    } else {
                                        Log.e(TAG, "Failed to update password", passwordTask.exception)
                                        handleUpdateResult(false, "Gagal mengubah password")
                                    }
                                }
                        } else {
                            Log.e(TAG, "Re-authentication failed", reAuthTask.exception)
                            handleUpdateResult(false, "Password saat ini salah")
                        }
                    }
            } else {
                // No password change, just update profile
                updateProfileData(currentUser, newName, newEmail)
            }
        } else {
            handleUpdateResult(false, "User tidak terautentikasi")
        }
    }

    private fun updateProfileData(currentUser: com.google.firebase.auth.FirebaseUser, newName: String, newEmail: String) {
        Log.d(TAG, "Starting profile update - Image changed: $isImageChanged")

        if (isImageChanged && selectedImageUri != null) {
            // Simpan gambar lokal
            val localPath = saveImageToInternalStorage(selectedImageUri!!)
            if (localPath != null) {
                Log.d(TAG, "Image saved locally: $localPath")
                updateUserProfile(currentUser, newName, newEmail, localPath)
            } else {
                Log.e(TAG, "Failed to save image locally")
                handleUpdateResult(false, "Gagal menyimpan foto profil secara lokal")
            }
        } else {
            Log.d(TAG, "No image change, updating profile without new image")
            updateUserProfile(currentUser, newName, newEmail, null)
        }
    }


    // PERBAIKAN UTAMA: Fungsi upload gambar yang diperbaiki
    private fun uploadProfileImage(userName: String, callback: (String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "Current user is null")
            callback(null)
            return
        }

        val selectedUri = selectedImageUri
        if (selectedUri == null) {
            Log.e(TAG, "Selected image URI is null")
            callback(null)
            return
        }

        Log.d(TAG, "Starting image upload to Firebase Storage...")
        Log.d(TAG, "Selected URI: $selectedUri")

        try {
            // Sanitize username for file naming (remove special characters)
            val sanitizedUserName = userName.replace(Regex("[^a-zA-Z0-9]"), "_")

            // Create timestamp for unique filename
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            // Create filename: userid_timestamp.jpg (menggunakan user ID untuk keunikan)
            val fileName = "${currentUser.uid}_${timestamp}.jpg"

            // Create a reference to store the image
            val imageRef: StorageReference = storage.reference
                .child("profile_images")
                .child(currentUser.uid) // Folder berdasarkan user ID
                .child(fileName)

            Log.d(TAG, "Upload path: ${imageRef.path}")
            Log.d(TAG, "Filename: $fileName")

            // Start upload
            val uploadTask = imageRef.putFile(selectedUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                Log.d(TAG, "Upload progress: $progress%")

                // Update UI with progress
                runOnUiThread {
                    saveButton.text = "Mengupload... ${progress.toInt()}%"
                }
            }.addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "Image uploaded successfully")

                // Get download URL
                imageRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        val downloadUrlString = downloadUrl.toString()
                        Log.d(TAG, "Download URL obtained: $downloadUrlString")

                        // Delete old profile images (optional)
                        deleteOldProfileImages(currentUser.uid)

                        callback(downloadUrlString)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to get download URL", exception)
                        callback(null)
                    }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Failed to upload image", exception)
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.upload_image_failed, exception.message ?: "Unknown error"), Toast.LENGTH_LONG).show()
                }
                callback(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception during image upload", e)
            callback(null)
        }
    }

    private fun deleteOldProfileImages(userId: String) {
        // Optional: Delete old profile images to save storage space
        val userFolderRef = storage.reference.child("profile_images").child(userId)

        userFolderRef.listAll()
            .addOnSuccessListener { listResult ->
                // Jangan hapus file yang baru saja diupload
                val currentTime = System.currentTimeMillis()

                for (item in listResult.items) {
                    // Hanya hapus file yang lebih lama dari 1 menit
                    item.metadata.addOnSuccessListener { metadata ->
                        val fileTime = metadata.creationTimeMillis
                        if (currentTime - fileTime > 60000) { // 1 menit
                            item.delete()
                                .addOnSuccessListener {
                                    Log.d(TAG, "Old image deleted: ${item.name}")
                                }
                                .addOnFailureListener { exception ->
                                    Log.w(TAG, "Failed to delete old image: ${item.name}", exception)
                                }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Failed to list old images", exception)
            }
    }

    private fun updateUserProfile(currentUser: com.google.firebase.auth.FirebaseUser, newName: String, newEmail: String, imagePath: String?) {
        Log.d(TAG, "Updating user profile - Name: $newName, Email: $newEmail, ImagePath: $imagePath")

        val profileUpdatesBuilder = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)

        val profileUpdates = profileUpdatesBuilder.build()

        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener { profileTask ->
                if (profileTask.isSuccessful) {
                    Log.d(TAG, "User profile updated successfully")

                    // PERBAIKAN: Simpan path gambar ke SharedPreferences setelah berhasil update
                    if (!imagePath.isNullOrEmpty()) {
                        saveProfileImagePath(imagePath)
                        Log.d(TAG, "Profile image path saved: $imagePath")
                    }

                    if (newEmail != currentUser.email) {
                        currentUser.updateEmail(newEmail)
                            .addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    handleUpdateResult(true, null)
                                } else {
                                    handleUpdateResult(false, "Gagal mengubah email: ${emailTask.exception?.message}")
                                }
                            }
                    } else {
                        handleUpdateResult(true, null)
                    }
                } else {
                    handleUpdateResult(false, "Gagal memperbarui profil: ${profileTask.exception?.message}")
                }
            }

        // Update ImageView langsung dari file lokal
        if (!imagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(File(imagePath))
                .centerCrop()
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(profileImage)
        }
    }


    private fun handleUpdateResult(success: Boolean, errorMessage: String? = null) {
        // Restore button state
        saveButton.isEnabled = true
        saveButton.text = "Simpan"

        if (success) {
            Log.d(TAG, "Profile update completed successfully")
            Toast.makeText(this, getString(R.string.data_updated_success), Toast.LENGTH_SHORT).show()

            // Return to Account activity with result
            val resultIntent = Intent()
            resultIntent.putExtra("data_updated", true)
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            val message = errorMessage ?: "Gagal memperbarui data"
            Log.e(TAG, "Profile update failed: $message")
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, login_form::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is still signed in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToLogin()
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        try {
            // Convert URI to Bitmap
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            // Folder penyimpanan di internal storage
            val filename = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, filename)

            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
            }

            Log.d(TAG, "Image saved locally: ${file.absolutePath}")
            return file.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image locally", e)
            return null
        }
    }

    private fun saveProfileImagePath(path: String) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().putString("profile_image_path", path).apply()
    }

    private fun getProfileImagePath(): String? {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getString("profile_image_path", null)
    }



}