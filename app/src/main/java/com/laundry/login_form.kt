// Updated login_form.kt with Google Sign-In for both login and registration
package com.laundry

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class login_form : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // UI Components
    private lateinit var phoneInputField: TextInputEditText
    private lateinit var passwordInputField: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var googleLoginButton: MaterialButton

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_form)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize UI components
        initViews()

        // Configure Google Sign In
        configureGoogleSignIn()

        // Set click listeners
        setupClickListeners()
    }

    private fun initViews() {
        phoneInputField = findViewById(R.id.phoneInputField)
        passwordInputField = findViewById(R.id.passwordInputField)
        loginButton = findViewById(R.id.loginButton)
        googleLoginButton = findViewById(R.id.googleLoginButton)
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            val phone = phoneInputField.text.toString().trim()
            val password = passwordInputField.text.toString().trim()

            if (validateInputs(phone, password)) {
                loginWithEmailPassword(phone, password)
            }
        }

        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }

        // Handle register link click
        findViewById<View>(R.id.registerText).setOnClickListener {
            // Navigate to register activity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Handle forgot password click
        findViewById<View>(R.id.forgotPasswordText).setOnClickListener {
            val phone = phoneInputField.text.toString().trim()
            if (phone.isNotEmpty()) {
                resetPassword(phone)
            } else {
                Toast.makeText(this, "Masukkan nomor handphone terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(phone: String, password: String): Boolean {
        if (phone.isEmpty()) {
            phoneInputField.error = "Nomor handphone tidak boleh kosong"
            return false
        }

        if (!isValidPhoneNumber(phone)) {
            phoneInputField.error = "Format nomor handphone tidak valid"
            return false
        }

        if (password.isEmpty()) {
            passwordInputField.error = "Password tidak boleh kosong"
            return false
        }

        if (password.length < 6) {
            passwordInputField.error = "Password minimal 6 karakter"
            return false
        }

        return true
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Indonesian phone number validation (08xxxxxxxxxx)
        return phone.matches(Regex("^08[0-9]{8,11}$"))
    }

    private fun loginWithEmailPassword(phone: String, password: String) {
        showLoading(true)

        // Convert phone to email format for Firebase Auth
        val email = "$phone@laundryapp.com"

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!, account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google Sign In gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, account: GoogleSignInAccount) {
        showLoading(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    if (isNewUser) {
                        // This is a new user, update profile with Google account info
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(account.displayName)
                            .setPhotoUri(account.photoUrl)
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    Log.d(TAG, "User profile updated with Google info")
                                }
                                Toast.makeText(this, "Akun baru dibuat dengan Google", Toast.LENGTH_SHORT).show()
                                navigateToMainActivity()
                            }
                    } else {
                        Toast.makeText(this, "Login dengan Google berhasil", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resetPassword(phone: String) {
        val email = "$phone@laundryapp.com"

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email reset password telah dikirim", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal mengirim email reset password", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            loginButton.text = "Loading..."
            loginButton.isEnabled = false
            googleLoginButton.isEnabled = false
        } else {
            loginButton.text = "Masuk"
            loginButton.isEnabled = true
            googleLoginButton.isEnabled = true
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, Beranda_laundry::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToMainActivity()
        }
    }
}