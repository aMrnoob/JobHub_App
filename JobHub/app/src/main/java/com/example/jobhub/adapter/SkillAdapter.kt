package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobhub.R
import com.example.jobhub.dto.jobseeker.SkillInfo

class SkillAdapter(private var skills: MutableList<SkillInfo>, private var isEditable: Boolean = false) :
    RecyclerView.Adapter<SkillAdapter.SkillViewHolder>() {

    inner class SkillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val edtSkill: EditText = itemView.findViewById(R.id.edtSkill)
        private val btnRemoveSkill: ImageView = itemView.findViewById(R.id.btnRemoveSkill)

        fun bind(skill: SkillInfo) {
            edtSkill.setText(skill.skillName)
            edtSkill.isEnabled = isEditable

            btnRemoveSkill.isEnabled = isEditable
            btnRemoveSkill.visibility = if (isEditable) View.VISIBLE else View.GONE

            edtSkill.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        skills[pos].skillName = s.toString()
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            btnRemoveSkill.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    skills.removeAt(pos)
                    notifyItemRemoved(pos)
                    notifyItemRangeChanged(pos, skills.size)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_skill, parent, false)
        return SkillViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        holder.bind(skills[position])
    }

    override fun getItemCount(): Int = skills.size

    fun addSkill(skill: SkillInfo) {
        skills.add(skill)
        notifyItemInserted(skills.size - 1)
    }

    fun getSkills(): List<SkillInfo> {
        return skills.filter { it.skillName.isNotBlank() }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEditable(editable: Boolean) {
        isEditable = editable
        notifyDataSetChanged()
    }
}
