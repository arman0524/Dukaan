package com.shopaccounting

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shopaccounting.database.AppDatabase
import com.shopaccounting.databinding.ActivityProfileBinding
import com.shopaccounting.models.ShopOwner
import com.shopaccounting.utils.PreferenceManager
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var database: AppDatabase
    private lateinit var preferenceManager: PreferenceManager
    private var currentShopOwner: ShopOwner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        preferenceManager = PreferenceManager(this)

        setupToolbar()
        setupClickListeners()
        loadProfileData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Shop Profile"
    }

    private fun setupClickListeners() {
        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadProfileData() {
        lifecycleScope.launch {
            try {
                currentShopOwner = database.shopOwnerDao().getShopOwner()
                currentShopOwner?.let { owner ->
                    binding.etOwnerName.setText(owner.ownerName)
                    binding.etShopName.setText(owner.shopName)
                    binding.etEmail.setText(owner.email)
                    binding.etContactNumber.setText(owner.contactNumber)
                    binding.etUpiId.setText(owner.upiId)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        val ownerName = binding.etOwnerName.text.toString().trim()
        val shopName = binding.etShopName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val contactNumber = binding.etContactNumber.text.toString().trim()
        val upiId = binding.etUpiId.text.toString().trim()

        if (ownerName.isEmpty() || shopName.isEmpty() || email.isEmpty() ||
            contactNumber.isEmpty() || upiId.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
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

        binding.btnSaveProfile.isEnabled = false
        binding.btnSaveProfile.text = "Saving..."

        lifecycleScope.launch {
            try {
                currentShopOwner?.let { owner ->
                    val updatedOwner = owner.copy(
                        ownerName = ownerName,
                        shopName = shopName,
                        email = email,
                        contactNumber = contactNumber,
                        upiId = upiId
                    )

                    database.shopOwnerDao().updateShopOwner(updatedOwner)
                    preferenceManager.saveShopOwnerDetails(updatedOwner)

                    Toast.makeText(this@ProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSaveProfile.isEnabled = true
                binding.btnSaveProfile.text = "Save Changes"
            }
        }
    }
}
