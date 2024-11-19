package com.kontest2.activities

import android.view.LayoutInflater
import com.kontest2.R
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContestListAdapter(private val listener:ContestItemClicked): RecyclerView.Adapter<ContestViewHolder>() {


    private val items: ArrayList<Contest> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contest_view_layout, parent, false)
        val viewHolder = ContestViewHolder(view)
        view.setOnClickListener{
            listener.onContClicked(items[viewHolder.adapterPosition])
        }
        return viewHolder
    }
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ContestViewHolder, position: Int) {
        val currentItem = items[position]
//        holder.site_name.text = currentItem.site_name
        holder.cont_name.text = currentItem.cont_name
        holder.start_time.text = currentItem.start_time
//        holder.end_time.text = currentItem.end_time
    }

    fun updateContest(updatedContest: ArrayList<Contest>) {
        items.clear()
        items.addAll(updatedContest)

        notifyDataSetChanged()
    }
}

class ContestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//    val site_name: TextView = itemView.findViewById(R.id.site_name)
    val cont_name: TextView = itemView.findViewById(R.id.cont_name)
    val start_time: TextView = itemView.findViewById(R.id.start_time)
//    val end_time: TextView = itemView.findViewById(R.id.end_time)

}

interface ContestItemClicked {
    fun onContClicked(item: Contest)
}