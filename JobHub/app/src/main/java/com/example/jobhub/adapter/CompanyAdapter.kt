package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.entity.Company

class CompanyAdapter(private var companyList: List<Company>) :

    RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder>() {

    private var expandedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_company, parent, false)
        return CompanyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = companyList[position]
        holder.tvCompanyName.text = company.companyName
        holder.tvAddress.text = company.address
        holder.tvDescription.text = company.description

        Glide.with(holder.itemView.context)
            .load(company.logoUrl)
            .placeholder(R.drawable.error_image)
            .error(R.drawable.error_image)
            .into(holder.ivImgCompany)

        val isExpanded = position == expandedPosition
        holder.tvDescription.visibility = if (isExpanded) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val previousExpandedPosition = expandedPosition
            expandedPosition = if (isExpanded) RecyclerView.NO_POSITION else position

            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(expandedPosition)
        }
    }

    override fun getItemCount(): Int = companyList.size

    class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImgCompany: ImageView = itemView.findViewById(R.id.ivImgCompany)
        val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Company>) {
        companyList = newList
        notifyDataSetChanged()
    }
}