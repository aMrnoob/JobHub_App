package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobhub.R
import com.example.jobhub.dto.jobseeker.SkillInfo

class SkillAdapter(private var skills: MutableList<SkillInfo>) :
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
        val skill = skills[position]
        holder.edtSkill.setText(skill.skillName)

        holder.btnRemove.setOnClickListener {
            skills.removeAt(position)
            notifyDataSetChanged()
        }

        holder.edtSkill.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                skills[position].skillName = holder.edtSkill.text.toString().trim()
            }
        }
    }

    override fun getItemCount() = skills.size

    @SuppressLint("NotifyDataSetChanged")
    fun addSkill(skill: SkillInfo) {
        skills.add(skill)
        notifyDataSetChanged()
    }

    fun getSkills(): List<SkillInfo> = skills
        .filter { it.skillName.isNotBlank() }
        .mapIndexed { index, skill ->
            SkillInfo(skillId = index + 1, skillName = skill.skillName)
        }
}
