package com.nikita.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nikita.app.data.Category
import com.nikita.app.data.CategoryTotal
import com.nikita.app.databinding.CategorySummaryItemBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * RecyclerView adapter for category summary items
 */
class CategorySummaryAdapter(
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategorySummaryAdapter.CategoryViewHolder>() {
    
    private var categories = listOf<CategoryTotal>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("sq", "AL"))
    
    fun submitList(newCategories: List<CategoryTotal>) {
        categories = newCategories
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = CategorySummaryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }
    
    override fun getItemCount(): Int = categories.size
    
    inner class CategoryViewHolder(
        private val binding: CategorySummaryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(categoryTotal: CategoryTotal) {
            if (categoryTotal.category.iconDrawable != null) {
                binding.categoryIcon.visibility = android.view.View.GONE
                binding.categoryIconDrawable.visibility = android.view.View.VISIBLE
                binding.categoryIconDrawable.setImageResource(categoryTotal.category.iconDrawable)
            } else {
                binding.categoryIcon.visibility = android.view.View.VISIBLE
                binding.categoryIconDrawable.visibility = android.view.View.GONE
                binding.categoryIcon.text = categoryTotal.category.iconRes
            }
            binding.categoryName.text = categoryTotal.category.displayName
            binding.categoryAmount.text = com.nikita.app.utils.CurrencyManager.formatAmount(binding.root.context, categoryTotal.total)
            
            // Reduce text size for amounts over 1,000
            if (categoryTotal.total >= 1000) {
                binding.categoryAmount.textSize = 16f
            } else {
                binding.categoryAmount.textSize = 20f
            }
            
            binding.deleteButton.setOnClickListener {
                onDeleteClick(categoryTotal.category)
            }
        }
    }
}
