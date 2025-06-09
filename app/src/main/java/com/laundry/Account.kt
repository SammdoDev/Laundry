package com.laundry

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class Account : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // UI Components
    private lateinit var backButton: ImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var userName: TextView
    private lateinit var userPhone: TextView
    private lateinit var userEmail: TextView
    private lateinit var accountSettingsLayout: LinearLayout
    private lateinit var privacyPolicyLayout: LinearLayout
    private lateinit var helpSupportLayout: LinearLayout
    private lateinit var logoutButton: MaterialButton
    private lateinit var editButton: ImageView

    companion object {
        private const val TAG = "AccountActivity"
        private const val EDIT_ACCOUNT_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Configure Google Sign In
        configureGoogleSignIn()

        // Initialize UI components
        initViews()

        // Load user data
        loadUserData()

        // Set click listeners
        setupClickListeners()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profileImage)
        userName = findViewById(R.id.userName)
        userPhone = findViewById(R.id.userPhone)
        userEmail = findViewById(R.id.userEmail)
        accountSettingsLayout = findViewById(R.id.accountSettingsLayout)
        privacyPolicyLayout = findViewById(R.id.privacyPolicyLayout)
        helpSupportLayout = findViewById(R.id.helpSupportLayout)
        logoutButton = findViewById(R.id.logoutButton)
        editButton = findViewById(R.id.editButton)
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "Loading user data for: ${currentUser.uid}")

            // Set user name
            val displayName = currentUser.displayName
            if (!displayName.isNullOrEmpty()) {
                userName.text = displayName
                Log.d(TAG, "Display name: $displayName")
            } else {
                userName.text = "User"
                Log.d(TAG, "No display name available")
            }

            // Set user email
            val email = currentUser.email
            if (!email.isNullOrEmpty()) {
                userEmail.text = email
                Log.d(TAG, "Email: $email")

                // Extract phone number if email format is phone@laundryapp.com
                if (email.endsWith("@laundryapp.com")) {
                    val phone = email.substringBefore("@laundryapp.com")
                    if (phone.matches(Regex("^08[0-9]{8,11}$"))) {
                        userPhone.text = phone
                        Log.d(TAG, "Phone extracted from email: $phone")
                    } else {
                        userPhone.text = "Tidak tersedia"
                        Log.d(TAG, "Invalid phone format in email")
                    }
                } else {
                    userPhone.text = "Tidak tersedia"
                    Log.d(TAG, "Email not in phone@laundryapp.com format")
                }
            } else {
                userEmail.text = "Tidak tersedia"
                userPhone.text = "Tidak tersedia"
                Log.d(TAG, "No email available")
            }

            // Load profile image if available
            loadProfileImage(currentUser)
        } else {
            Log.d(TAG, "No current user, redirecting to login")
            // User not logged in, redirect to login
            navigateToLogin()
        }
    }

    private fun loadProfileImage(currentUser: com.google.firebase.auth.FirebaseUser) {
        val localImagePath = getProfileImagePath()
        Log.d(TAG, "localImagePath = $localImagePath")

        if (!localImagePath.isNullOrEmpty()) {
            val file = File(localImagePath)
            Log.d(TAG, "File exists: ${file.exists()}, path: $localImagePath")

            if (file.exists()) {
                // PERBAIKAN: Tambahkan error handling yang lebih baik
                Glide.with(this)
                    .load(file)
                    .centerCrop()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Hindari cache untuk file lokal
                    .skipMemoryCache(true)
                    .into(profileImage)
                Log.d(TAG, "Loading local image file successfully")
            } else {
                Log.d(TAG, "Local image file not found, clearing saved path")
                // PERBAIKAN: Hapus path yang tidak valid dari SharedPreferences
                clearProfileImagePath()
                loadFirebaseProfileImage(currentUser)
            }
        } else {
            Log.d(TAG, "No local image path found, trying Firebase photo URL")
            loadFirebaseProfileImage(currentUser)
        }
    }


    private fun getProfileImagePath(): String? {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getString("profile_image_path", null)
    }



    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        accountSettingsLayout.setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_pengaturan_akun), Toast.LENGTH_SHORT).show()
        }

        privacyPolicyLayout.setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_kebijakan_privasi), Toast.LENGTH_SHORT).show()
        }

        helpSupportLayout.setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_bantuan_dukungan), Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        editButton.setOnClickListener {
            val intent = Intent(this, edit_account::class.java)
            startActivityForResult(intent, EDIT_ACCOUNT_REQUEST)
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        try {
            // Sign out from Firebase
            auth.signOut()

            // Sign out from Google
            googleSignInClient.signOut().addOnCompleteListener(this) {
                Log.d(TAG, "Google sign out completed")

                // Clear Glide image caches to remove cached profile images
                Glide.get(this).clearMemory()
                Thread {
                    Glide.get(this).clearDiskCache()
                }.start()

                Toast.makeText(this, getString(R.string.msg_keluar_berhasil), Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            Toast.makeText(this, getString(R.string.msg_keluar_gagal), Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()
        // Reload user data when returning to this activity
        loadUserData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult called with requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == EDIT_ACCOUNT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Edit account success, reloading user data")
                loadUserData()
            } else {
                Log.d(TAG, "Edit account canceled or failed")
            }
        }
    }

    private fun loadFirebaseProfileImage(currentUser: com.google.firebase.auth.FirebaseUser) {
        val photoUrl = currentUser.photoUrl
        Log.d(TAG, "Photo URL from Firebase: $photoUrl")

        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(profileImage)
        } else {
            Log.d(TAG, "No photo URL found, set default placeholder")
            profileImage.setImageResource(R.drawable.ic_person_placeholder)
        }
    }

    private fun clearProfileImagePath() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().remove("profile_image_path").apply()
        Log.d(TAG, "Cleared invalid profile image path")
    }

    private fun saveProfileImagePath(path: String) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().putString("profile_image_path", path).apply()
    }
}
