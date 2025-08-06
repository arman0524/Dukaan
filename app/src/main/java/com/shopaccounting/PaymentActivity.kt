package com.shopaccounting

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.shopaccounting.database.AppDatabase
import com.shopaccounting.databinding.ActivityPaymentBinding
import com.shopaccounting.models.PaymentMethod
import com.shopaccounting.models.Transaction
import com.shopaccounting.models.TransactionType
import com.shopaccounting.utils.NotificationHelper
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var database: AppDatabase
    private lateinit var notificationHelper: NotificationHelper
    private var customerId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        notificationHelper = NotificationHelper(this)
        customerId = intent.getLongExtra("customer_id", 0)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnGenerateQR.setOnClickListener {
            generateUPIQR()
        }

        binding.btnCashPayment.setOnClickListener {
            processCashPayment()
        }

        binding.btnConfirmUPIPayment.setOnClickListener {
            processUPIPayment()
        }
    }

    private fun generateUPIQR() {
        val amount = binding.etPaymentAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Replace with your UPI ID
        val upiId = "armandubey2411@oksb" // Your UPI ID
        val name = "Arman Dubey"
        val note = "Payment for borrowed goods"
        
        val upiUrl = "upi://pay?pa=$upiId&pn=$name&am=$amount&cu=INR&tn=$note"
        
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(upiUrl, BarcodeFormat.QR_CODE, 400, 400)
            binding.ivQRCode.setImageBitmap(bitmap)
            binding.ivQRCode.visibility = android.view.View.VISIBLE
            binding.btnConfirmUPIPayment.visibility = android.view.View.VISIBLE
        } catch (e: WriterException) {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processCashPayment() {
        val amount = binding.etPaymentAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            customerId = customerId,
            amount = amount,
            type = TransactionType.PAYMENT,
            paymentMethod = PaymentMethod.CASH,
            description = "Cash payment received"
        )

        lifecycleScope.launch {
            try {
                database.transactionDao().insertTransaction(transaction)
                val customer = database.customerDao().getCustomerById(customerId)
                customer?.let {
                    notificationHelper.sendPaymentNotification(it, amount, PaymentMethod.CASH)
                }
                Toast.makeText(this@PaymentActivity, "Cash payment recorded successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@PaymentActivity, "Error recording payment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processUPIPayment() {
        val amount = binding.etPaymentAmount.text.toString().toDoubleOrNull()
        val transactionId = binding.etUPITransactionId.text.toString().trim()
        
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (transactionId.isEmpty()) {
            Toast.makeText(this, "Please enter UPI transaction ID", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            customerId = customerId,
            amount = amount,
            type = TransactionType.PAYMENT,
            paymentMethod = PaymentMethod.UPI,
            description = "UPI payment received",
            upiTransactionId = transactionId
        )

        lifecycleScope.launch {
            try {
                database.transactionDao().insertTransaction(transaction)
                val customer = database.customerDao().getCustomerById(customerId)
                customer?.let {
                    notificationHelper.sendPaymentNotification(it, amount, PaymentMethod.UPI)
                }
                Toast.makeText(this@PaymentActivity, "UPI payment recorded successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@PaymentActivity, "Error recording payment", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
