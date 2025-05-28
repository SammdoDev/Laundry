package com.laundry

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var nameInputField: TextInputEditText
    private lateinit var phoneInputField: TextInputEditText
    private lateinit var passwordInputField: TextInputEditText
    private lateinit var confirmPasswordInputField: TextInputEditText
    private lateinit var registerButton: MaterialButton

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize UI components
        initViews()

        // Set click listeners
        setupClickListeners()
    }

    private fun initViews() {
        nameInputField = findViewById(R.id.nameInputField)
        phoneInputField = findViewById(R.id.phoneInputField)
        passwordInputField = findViewById(R.id.passwordInputField)
        confirmPasswordInputField = findViewById(R.id.confirmPasswordInputField)
        registerButton = findViewById(R.id.registerButton)
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
    }

    private fun validateInputs(name: String, phone: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            nameInputField.error = "Nama tidak boleh kosong"
            return false
        }

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

        if (confirmPassword != password) {
            confirmPasswordInputField.error = "Password tidak sama"
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
                                Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()

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
                    Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            registerButton.text = "Loading..."
            registerButton.isEnabled = false
        } else {
            registerButton.text = "Daftar"
            registerButton.isEnabled = true
        }
    }
}