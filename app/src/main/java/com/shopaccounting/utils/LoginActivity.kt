package com.shopaccounting

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shopaccounting.database.AppDatabase
import com.shopaccounting.databinding.ActivityLoginBinding
import com.shopaccounting.utils.PreferenceManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: AppDatabase
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        preferenceManager = PreferenceManager(this)

        // Check if user is already logged in
        if (preferenceManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupClickListeners()
        checkFirstTimeSetup()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkFirstTimeSetup() {
        lifecycleScope.launch {
            val ownerCount = database.shopOwnerDao().getShopOwnerCount()
            if (ownerCount == 0) {
                // Show message for first time users
                binding.tvFirstTimeMessage.visibility = android.view.View.VISIBLE
                binding.tvFirstTimeMessage.text = "Welcome! Please register your shop to get started."
            } else {
                binding.tvFirstTimeMessage.visibility = android.view.View.GONE
            }
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."

        lifecycleScope.launch {
            try {
                val shopOwner = database.shopOwnerDao().login(email, password)
                if (shopOwner != null) {
                    preferenceManager.saveLoginState(true, shopOwner.id)
                    preferenceManager.saveShopOwnerDetails(shopOwner)
                    Toast.makeText(this@LoginActivity, "Welcome back, ${shopOwner.ownerName}!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Login"
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

