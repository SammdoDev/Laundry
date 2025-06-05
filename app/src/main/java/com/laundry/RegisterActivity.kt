package com.laundry

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.widget.TextView



class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // UI Components
    private lateinit var nameInputField: TextInputEditText
    private lateinit var phoneInputField: TextInputEditText
    private lateinit var passwordInputField: TextInputEditText
    private lateinit var confirmPasswordInputField: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var googleRegisterButton: SignInButton


    companion object {
        private const val TAG = "RegisterActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Configure Google Sign In
        configureGoogleSignIn()

        // Initialize UI components
        initViews()

        // Set click listeners
        setupClickListeners()

        val masuk: TextView = findViewById(R.id.loginLink)
        masuk.setOnClickListener {
            val intent = Intent(this, login_form::class.java)
            startActivity(intent)
        }


    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initViews() {
        nameInputField = findViewById(R.id.nameInputField)
        phoneInputField = findViewById(R.id.phoneInputField)
        passwordInputField = findViewById(R.id.passwordInputField)
        confirmPasswordInputField = findViewById(R.id.confirmPasswordInputField)
        registerButton = findViewById(R.id.registerButton)
        googleRegisterButton = findViewById(R.id.googleSignInButton)
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            val name = nameInputField.text.toString().trim()
            val phone = phoneInputField.text.toString().trim()
            val password = passwordInputField.text.toString().trim()
            val confirmPassword = confirmPasswordInputField.text.toString().trim()

            if (validateInputs(name, phone, password, confirmPassword)) {
                registerUser(name, phone, password)
            }
        }

        googleRegisterButton.setOnClickListener {
            signUpWithGoogle()
        }
    }

    private fun validateInputs(name: String, phone: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            nameInputField.error = getString(R.string.error_name_empty)
            return false
        }

        if (phone.isEmpty()) {
            phoneInputField.error = getString(R.string.error_phone_empty)
            return false
        }

        if (!isValidPhoneNumber(phone)) {
            phoneInputField.error = getString(R.string.error_phone_invalid)
            return false
        }

        if (password.isEmpty()) {
            passwordInputField.error = getString(R.string.error_password_empty)
            return false
        }

        if (password.length < 6) {
            passwordInputField.error = getString(R.string.error_password_short)
            return false
        }

        if (confirmPassword != password) {
            confirmPasswordInputField.error = getString(R.string.error_password_mismatch)
            return false
        }

        return true
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^08[0-9]{8,11}$"))
    }

    private fun registerUser(name: String, phone: String, password: String) {
        showLoading(true)

        // Convert phone to email format for Firebase Auth
        val email = "$phone@laundryapp.com"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser

                    // Update user profile with name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            showLoading(false)
                            if (profileTask.isSuccessful) {
                                Log.d(TAG, "User profile updated.")
                                Toast.makeText(this, getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show()

                                // Navigate to login
                                val intent = Intent(this, login_form::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                finish()
                            }
                        }
                } else {
                    showLoading(false)
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, getString(R.string.msg_register_failed, task.exception?.message), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun signUpWithGoogle() {
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
                Log.w(TAG, "Google sign up failed", e)
                Toast.makeText(this, getString(R.string.msg_google_signup_failed), Toast.LENGTH_SHORT).show()
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
                        // This is a new user registration
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(account.displayName)
                            .setPhotoUri(account.photoUrl)
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    Log.d(TAG, "User profile updated with Google info")
                                }
                                Toast.makeText(this, getString(R.string.msg_google_signup_success), Toast.LENGTH_SHORT).show()
                                navigateToLogin()
                            }
                    } else {
                        // User already exists, redirect to login
                        Toast.makeText(this, getString(R.string.msg_account_exists), Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, getString(R.string.msg_auth_failed), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            registerButton.text = "Loading..."
            registerButton.isEnabled = false
            googleRegisterButton.isEnabled = false
        } else {
            registerButton.text = getString(R.string.btn_register)
            registerButton.isEnabled = true
            googleRegisterButton.isEnabled = true
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, login_form::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }


}