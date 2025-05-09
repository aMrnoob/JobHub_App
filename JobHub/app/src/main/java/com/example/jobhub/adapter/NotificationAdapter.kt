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
    private val listener: OnNotificationListener
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    interface OnNotificationListener {
        fun onItemClick(notification: NotificationEntityDTO)
        fun onViewDetailClick(notification: NotificationEntityDTO)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvSenderName.text = notification.receiver?.fullName ?: "System"
        val rawDate = notification.createdAt
        val formattedDate = rawDate?.replace("T", " ")?.dropLast(7)
        holder.tvDate.text = formattedDate
        holder.tvContent.text = notification.content

        if (!notification.read) {
            holder.tvUnread.visibility = View.VISIBLE
        } else {
            holder.tvUnread.visibility = View.GONE
        }

        holder.tvAction.setOnClickListener {
            notification.read = true
            listener.onViewDetailClick(notification)
        }

        holder.itemView.setOnClickListener {
            notification.read = true
            listener.onItemClick(notification)
        }
    }

    override fun getItemCount(): Int = notifications.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val tvUnread: TextView = itemView.findViewById(R.id.tvUnread)
        val tvAction: TextView = itemView.findViewById(R.id.tvAction)
    }
}