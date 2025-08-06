package com.shopaccounting.utils

import android.content.Context
import android.telephony.SmsManager
import android.widget.Toast
import com.shopaccounting.models.Customer
import com.shopaccounting.models.PaymentMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class NotificationHelper(private val context: Context) {
    private val preferenceManager = PreferenceManager(context)

    fun sendPaymentNotification(customer: Customer, amount: Double, paymentMethod: PaymentMethod) {
        val ownerName = preferenceManager.getOwnerName()
        val shopName = preferenceManager.getShopName()
        val ownerContact = preferenceManager.getOwnerContact()

        val message = "Dear ${customer.name},\n\n" +
                "Your payment of ₹${String.format("%.2f", amount)} via ${paymentMethod.name} " +
                "has been received successfully.\n\n" +
                "Thank you for your business!\n\n" +
                "Best regards,\n$ownerName\n$shopName\n" +
                "Contact: $ownerContact"

        // Send SMS
        sendSMS(customer.phoneNumber, message)

        // Send Email if available
        customer.email?.let { email ->
            sendEmail(email, "Payment Confirmation - $shopName", message)
        }
    }

    fun sendBorrowNotification(customer: Customer, amount: Double) {
        val ownerName = preferenceManager.getOwnerName()
        val shopName = preferenceManager.getShopName()
        val ownerContact = preferenceManager.getOwnerContact()
        val upiId = preferenceManager.getUpiId()

        val message = "Dear ${customer.name},\n\n" +
                "You have borrowed ₹${String.format("%.2f", amount)} from $shopName.\n\n" +
                "Please pay when convenient.\n" +
                "UPI ID for payment: $upiId\n\n" +
                "For any queries, contact: $ownerContact\n\n" +
                "Best regards,\n$ownerName\n$shopName"

        // Send SMS
        sendSMS(customer.phoneNumber, message)

        // Send Email if available
        customer.email?.let { email ->
            sendEmail(email, "Borrow Amount Added - $shopName", message)
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail(email: String, subject: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val senderEmail = preferenceManager.getOwnerEmail()
                val senderPassword = "your-app-password" // Shop owner needs to set this up

                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(senderEmail, senderPassword)
                    }
                })

                val mimeMessage = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                    this.subject = subject
                    setText(message)
                }

                Transport.send(mimeMessage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

