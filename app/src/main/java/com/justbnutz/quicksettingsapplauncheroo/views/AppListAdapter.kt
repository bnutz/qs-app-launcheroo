package com.justbnutz.quicksettingsapplauncheroo.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.justbnutz.quicksettingsapplauncheroo.databinding.ListitemApplistBinding
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AppListAdapter(private val lifecycleScope: CoroutineScope) : RecyclerView.Adapter<AppListAdapter.AppItemViewHolder>() {

    interface AppListAdapterCallback {
        fun onItemClick(appData: AppItemModel)
        fun onItemLongClick(packageName: String)
    }

    private var parentCallback: AppListAdapterCallback? = null

    fun setAdapterListener(adapterCallback: AppListAdapterCallback) {
        parentCallback = adapterCallback
    }

    private var dataList = listOf<AppItemModel>()
    private val pendingUpdates = ArrayDeque<List<AppItemModel>>()

    fun updateItems(newList: List<AppItemModel>) {
        pendingUpdates.add(newList)

        // Only initiate a run if this is the only item in the queue, (otherwise it means an earlier list is currently running)
        pendingUpdates.singleOrNull()?.let {
            updateItemsInternal(it)
        }
    }

    private fun updateItemsInternal(nextList: List<AppItemModel>) {
        // === Background Thread ===
        lifecycleScope.launch {
            val callback = DiffUtilCallback(dataList, nextList)
            val diffResult = DiffUtil.calculateDiff(callback, true)

            // Discard everything that was inserted before the current item (so current item is now first in the queue)
            while (pendingUpdates.indexOf(nextList) > 0) pendingUpdates.removeFirst()

            withContext(Dispatchers.Main) {
                updateItemsUi(nextList, diffResult)
            }
        }
    }

    private fun updateItemsUi(nextList: List<AppItemModel>, diffResult: DiffUtil.DiffResult) {
        // Use main thread to remove current item from the queue to maintain thread-safety
        pendingUpdates.removeFirst()

        // Update the list
        dataList = nextList
        diffResult.dispatchUpdatesTo(this)

        // If there have been additions to the queue while this was working, init on the latest added
        pendingUpdates.lastOrNull()?.let {
            updateItemsInternal(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemViewBinding = ListitemApplistBinding.inflate(inflater, parent, false)
        return AppItemViewHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        val itemData = dataList[position]
        holder.bind(itemData,
            { parentCallback?.onItemClick(itemData) },
            { parentCallback?.onItemLongClick(itemData.packageName) }
        )
    }

    override fun getItemCount() = dataList.size

    class AppItemViewHolder(private val itemViewBinding: ListitemApplistBinding) : RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bind(appData: AppItemModel, onItemClick: () -> Unit, onLongClick: () -> Unit) {
            itemViewBinding.apply {
                txtAppname.text = appData.appName
                txtPackagename.text = appData.packageName
                imgAppicon.setImageDrawable(appData.appIcon)

                root.setOnClickListener {
                    onItemClick.invoke()
                }

                root.setOnLongClickListener {
                    onLongClick.invoke()
                    true
                }
            }
        }
    }

    private class DiffUtilCallback(
        val oldList: List<AppItemModel>,
        val newList: List<AppItemModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.packageName == newItem.packageName
                    && oldItem.appName == newItem.appName
                    && oldItem.appIcon?.hashCode() == newItem.appIcon?.hashCode()
                    && oldItem.isSystemApp == newItem.isSystemApp
        }
    }
}