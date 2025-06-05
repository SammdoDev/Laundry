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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

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

    companion object {
        private const val TAG = "AccountActivity"
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
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Set user name
            val displayName = currentUser.displayName
            if (!displayName.isNullOrEmpty()) {
                userName.text = displayName
            } else {
                userName.text = "User"
            }

            // Set user email
            val email = currentUser.email
            if (!email.isNullOrEmpty()) {
                userEmail.text = email

                // Extract phone number if email format is phone@laundryapp.com
                if (email.endsWith("@laundryapp.com")) {
                    val phone = email.substringBefore("@laundryapp.com")
                    if (phone.matches(Regex("^08[0-9]{8,11}$"))) {
                        userPhone.text = phone
                    } else {
                        userPhone.text = "Tidak tersedia"
                    }
                } else {
                    userPhone.text = "Tidak tersedia"
                }
            } else {
                userEmail.text = "Tidak tersedia"
                userPhone.text = "Tidak tersedia"
            }

            // Load profile image if available
            val photoUrl = currentUser.photoUrl
            if (photoUrl != null) {
                // Use image loading library like Glide or Picasso to load image
                // Glide.with(this).load(photoUrl).into(profileImage)
                Log.d(TAG, "Profile photo URL: $photoUrl")
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
}