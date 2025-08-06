package com.shopaccounting

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shopaccounting.database.AppDatabase
import com.shopaccounting.databinding.ActivityRegisterBinding
import com.shopaccounting.models.ShopOwner
import com.shopaccounting.utils.PreferenceManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: AppDatabase
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        preferenceManager = PreferenceManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            performRegistration()
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun performRegistration() {
        val ownerName = binding.etOwnerName.text.toString().trim()
        val shopName = binding.etShopName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val contactNumber = binding.etContactNumber.text.toString().trim()
        val upiId = binding.etUpiId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (ownerName.isEmpty() || shopName.isEmpty() || email.isEmpty() ||
            contactNumber.isEmpty() || upiId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (contactNumber.length < 10) {
            Toast.makeText(this, "Please enter a valid contact number", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating Account..."

        val shopOwner = ShopOwner(
            ownerName = ownerName,
            shopName = shopName,
            email = email,
            contactNumber = contactNumber,
            upiId = upiId,
            password = password
        )

        lifecycleScope.launch {
            try {
                val ownerId = database.shopOwnerDao().insertShopOwner(shopOwner)
                val savedOwner = shopOwner.copy(id = ownerId)

                preferenceManager.saveLoginState(true, ownerId)
                preferenceManager.saveShopOwnerDetails(savedOwner)

                Toast.makeText(this@RegisterActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Create Account"
            }
        }
    }
}
