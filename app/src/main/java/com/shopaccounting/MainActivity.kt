package com.shopaccounting

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopaccounting.adapters.CustomerAdapter
import com.shopaccounting.database.AppDatabase
import com.shopaccounting.databinding.ActivityMainBinding
import com.shopaccounting.utils.PreferenceManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var customerAdapter: CustomerAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        preferenceManager = PreferenceManager(this)

        // Check if user is logged in
        if (!preferenceManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeCustomers()
        updateWelcomeMessage()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val shopName = preferenceManager.getShopName()
        supportActionBar?.title = shopName.ifEmpty { "Shop Accounting" }
    }

    private fun setupRecyclerView() {
        customerAdapter = CustomerAdapter { customer ->
            val intent = Intent(this, CustomerDetailActivity::class.java)
            intent.putExtra("customer_id", customer.id)
            startActivity(intent)
        }

        binding.recyclerViewCustomers.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = customerAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddCustomer.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }

        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun updateWelcomeMessage() {
        val ownerName = preferenceManager.getOwnerName()
        val shopName = preferenceManager.getShopName()
        val ownerEmail = preferenceManager.getOwnerEmail()
        val ownerContact = preferenceManager.getOwnerContact()
        val upiId = preferenceManager.getUpiId()

        binding.tvWelcomeMessage.text = "Welcome back, $ownerName!"
        binding.tvShopName.text = shopName
        binding.tvOwnerEmail.text = ownerEmail
        binding.tvOwnerContact.text = ownerContact
        binding.tvUpiId.text = upiId
    }

    private fun observeCustomers() {
        lifecycleScope.launch {
            database.customerDao().getAllCustomers().collect { customers ->
                customerAdapter.submitList(customers)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        preferenceManager.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}



