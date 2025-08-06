package com.shopaccounting.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopaccounting.databinding.ItemTransactionBinding
import com.shopaccounting.models.Transaction
import com.shopaccounting.models.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            
            binding.tvAmount.text = "₹${String.format("%.2f", transaction.amount)}"
            binding.tvDescription.text = transaction.description ?: "No description"
            binding.tvDate.text = dateFormat.format(transaction.createdAt)
            binding.tvPaymentMethod.text = transaction.paymentMethod.name
            
            if (transaction.type == TransactionType.BORROW) {
                binding.tvAmount.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
                binding.tvType.text = "BORROWED"
            } else {
                binding.tvAmount.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
                binding.tvType.text = "PAID"
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
