package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.entity.Company
import com.example.jobhub.entity.enumm.CompanyAction

class CompanyAdapter(
    private var companyList: List<Company>,
    private val onActionClick: ((Company, CompanyAction) -> Unit)? = null
) : RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder>() {

    private var expandedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_company, parent, false)
        return CompanyViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CompanyViewHolder, @SuppressLint("RecyclerView") position: Int) {
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

            expandedPosition = if (isExpanded) {
                RecyclerView.NO_POSITION
            } else { position }

            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(expandedPosition)
            onActionClick?.let { it1 -> it1(company, CompanyAction.CLICK) }
        }

        holder.btnEdit.setOnClickListener {
            onActionClick?.let { it1 -> it1(company, CompanyAction.EDIT) }
        }

        holder.btnRemove.setOnClickListener {
            onActionClick?.let { it1 -> it1(company, CompanyAction.DELETE) }
        }
    }

    override fun getItemCount(): Int = companyList.size

    class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImgCompany: ImageView = itemView.findViewById(R.id.ivImgCompany)
        val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnEdit: LinearLayout = itemView.findViewById(R.id.btnEdit)
        val btnRemove: LinearLayout = itemView.findViewById(R.id.btnRemove)
    }
}