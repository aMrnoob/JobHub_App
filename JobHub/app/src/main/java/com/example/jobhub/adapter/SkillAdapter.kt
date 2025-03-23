package com.example.jobhub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobhub.R

class SkillAdapter(private val skills: MutableList<String>) :
    RecyclerView.Adapter<SkillAdapter.SkillViewHolder>() {

    class SkillViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val edtSkill: EditText = view.findViewById(R.id.edtSkill)
        val btnRemove: ImageView = view.findViewById(R.id.btnRemoveSkill)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_skill, parent, false)
        return SkillViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        holder.edtSkill.setText(skills[position])

        holder.btnRemove.setOnClickListener {
            skills.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = skills.size

    fun addSkill() {
        skills.add("")
        notifyDataSetChanged()
    }
}
