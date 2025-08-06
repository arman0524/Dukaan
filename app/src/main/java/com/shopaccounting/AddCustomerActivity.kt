package com.shopaccounting

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shopaccounting.database.AppDatabase
import com.shopaccounting.databinding.ActivityAddCustomerBinding
import com.shopaccounting.models.Customer
import kotlinx.coroutines.launch

class AddCustomerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCustomerBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSaveCustomer.setOnClickListener {
            saveCustomer()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveCustomer() {
        val name = binding.etCustomerName.text.toString().trim()
        val phone = binding.etPhoneNumber.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Name and phone number are required", Toast.LENGTH_SHORT).show()
            return
        }

        val customer = Customer(
            name = name,
            phoneNumber = phone,
            email = if (email.isEmpty()) null else email,
            address = if (address.isEmpty()) null else address
        )

        lifecycleScope.launch {
            try {
                database.customerDao().insertCustomer(customer)
                Toast.makeText(this@AddCustomerActivity, "Customer added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddCustomerActivity, "Error adding customer", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
