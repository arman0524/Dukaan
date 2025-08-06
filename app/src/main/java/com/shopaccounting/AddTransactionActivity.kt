package com.shopaccounting

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shopaccounting.database.AppDatabase
import com.shopaccounting.databinding.ActivityAddTransactionBinding
import com.shopaccounting.models.PaymentMethod
import com.shopaccounting.models.Transaction
import com.shopaccounting.models.TransactionType
import com.shopaccounting.utils.NotificationHelper
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var database: AppDatabase
    private lateinit var notificationHelper: NotificationHelper
    private var customerId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        notificationHelper = NotificationHelper(this)
        customerId = intent.getLongExtra("customer_id", 0)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSaveTransaction.setOnClickListener {
            saveTransaction()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveTransaction() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        val description = binding.etDescription.text.toString().trim()

        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            customerId = customerId,
            amount = amount,
            type = TransactionType.BORROW,
            paymentMethod = PaymentMethod.CASH,
            description = description.ifEmpty { "Borrowed goods" }
        )

        lifecycleScope.launch {
            try {
                database.transactionDao().insertTransaction(transaction)
                val customer = database.customerDao().getCustomerById(customerId)
                customer?.let {
                    notificationHelper.sendBorrowNotification(it, amount)
                }
                Toast.makeText(this@AddTransactionActivity, "Transaction added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddTransactionActivity, "Error adding transaction", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
