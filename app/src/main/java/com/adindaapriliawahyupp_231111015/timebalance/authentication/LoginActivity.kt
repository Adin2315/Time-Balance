package com.adindaapriliawahyupp_231111015.timebalance.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adindaapriliawahyupp_231111015.timebalance.MainActivity
import com.adindaapriliawahyupp_231111015.timebalance.data.SessionManager
import com.adindaapriliawahyupp_231111015.timebalance.database.DBAdapter
import com.adindaapriliawahyupp_231111015.timebalance.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var dbAdapter: DBAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        dbAdapter = DBAdapter(this)
        dbAdapter.open()

        // Auto login jika sudah login sebelumnya
        if (sessionManager.isLoggedIn()) {
            navigateToHome()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Email tidak boleh kosong"
            binding.etEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Format email tidak valid"
            binding.etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password tidak boleh kosong"
            binding.etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password minimal 6 karakter"
            binding.etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun performLogin(email: String, password: String) {
        val userId = dbAdapter.checkUser(email, password)
        if (userId != null) {
            sessionManager.setLoggedIn(true, userId)
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
            navigateToHome()
        } else {
            showError("Email atau password salah")
        }
    }


    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbAdapter.close()
    }
}
