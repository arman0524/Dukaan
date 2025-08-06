package com.shopaccounting

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopaccounting.adapters.TransactionAdapter
import com.shopaccounting.database.AppDatabase
import com.shopaccounting.databinding.ActivityCustomerDetailBinding
import com.shopaccounting.models.Customer
import kotlinx.coroutines.launch

class CustomerDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerDetailBinding
    private lateinit var database: AppDatabase
    private lateinit var transactionAdapter: TransactionAdapter
    private var customerId: Long = 0
    private var customer: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        customerId = intent.getLongExtra("customer_id", 0)

        setupRecyclerView()
        setupClickListeners()
        setupDeleteOption()
        loadCustomerData()
        observeTransactions()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(this@CustomerDetailActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddBorrow.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("customer_id", customerId)
            intent.putExtra("transaction_type", "BORROW")
            startActivity(intent)
        }

        binding.btnMakePayment.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("customer_id", customerId)
            startActivity(intent)
        }
    }

    private fun setupDeleteOption() {
        binding.btnDeleteCustomer.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Customer")
            .setMessage("Are you sure you want to delete this customer? This will also delete all transaction history.")
            .setPositiveButton("Delete") { _, _ ->
                deleteCustomer()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCustomer() {
        lifecycleScope.launch {
            try {
                customer?.let {
                    database.customerDao().deleteCustomer(it)
                    Toast.makeText(this@CustomerDetailActivity, "Customer deleted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomerDetailActivity, "Error deleting customer", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCustomerData() {
        lifecycleScope.launch {
            customer = database.customerDao().getCustomerById(customerId)
            customer?.let {
                binding.tvCustomerName.text = it.name
                binding.tvPhoneNumber.text = it.phoneNumber
                binding.tvEmail.text = it.email ?: "No email"
                updateBalance()
            }
        }
    }

    private fun updateBalance() {
        lifecycleScope.launch {
            val balance = database.transactionDao().getCustomerBalance(customerId) ?: 0.0
            binding.tvTotalBorrowed.text = "₹${String.format("%.2f", balance)}"
            
            // Update customer balance in database
            database.customerDao().updateCustomerBalance(customerId, balance)
        }
    }

    private fun observeTransactions() {
        lifecycleScope.launch {
            database.transactionDao().getTransactionsByCustomer(customerId).collect { transactions ->
                transactionAdapter.submitList(transactions)
                updateBalance()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateBalance()
    }
}
