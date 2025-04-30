package com.example.jobhub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobhub.R
import com.example.jobhub.dto.NotificationEntityDTO

class NotificationAdapter(
    private val notifications: List<NotificationEntityDTO>,
    private val onItemClickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    interface OnItemClickListener { fun onItemClick(notificationEntityDTO: NotificationEntityDTO) }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val tvUnread: TextView = itemView.findViewById(R.id.tvUnread)
        val tvAction: TextView = itemView.findViewById(R.id.tvAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvSenderName.text = notification.receiver?.fullName ?: "System"
        holder.tvDate.text = notification.createdAt
        holder.tvContent.text = notification.content
        if (!notification.isRead) {
            holder.tvUnread.visibility = View.VISIBLE
        } else {
            holder.tvUnread.visibility = View.GONE
        }

        holder.tvAction.setOnClickListener {
            onItemClickListener?.onItemClick(notification)
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(notification)
        }
    }

    override fun getItemCount(): Int = notifications.size
}