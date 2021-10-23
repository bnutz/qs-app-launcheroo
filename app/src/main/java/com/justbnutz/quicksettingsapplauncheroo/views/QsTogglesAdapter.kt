package com.justbnutz.quicksettingsapplauncheroo.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.justbnutz.quicksettingsapplauncheroo.R
import com.justbnutz.quicksettingsapplauncheroo.databinding.ListitemQsToggleBinding
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import com.justbnutz.quicksettingsapplauncheroo.models.ShortcutItemModel
import com.justbnutz.quicksettingsapplauncheroo.utils.PrefHelper
import com.justbnutz.quicksettingsapplauncheroo.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QsTogglesAdapter(private val lifecycleScope: CoroutineScope) : RecyclerView.Adapter<QsTogglesAdapter.QsToggleItemViewHolder>() {

    interface QsTogglesAdapterCallback {
        fun onQsAssign(serviceTag: String)
        fun onQsClear(serviceTag: String)
        fun onQsItemClick()
        fun onQsLongClick(packageName: String)
        fun onAppIconClick(appData: AppItemModel)
    }

    private var parentCallback: QsTogglesAdapterCallback? = null

    fun setAdapterListener(adapterCallback: QsTogglesAdapterCallback) {
        parentCallback = adapterCallback
    }

    private var dataList = listOf<ShortcutItemModel>()
    private val pendingUpdates = ArrayDeque<List<ShortcutItemModel>>()

    fun updateItems(newList: List<ShortcutItemModel>) {
        pendingUpdates.add(newList)

        // Only initiate a run if this is the only item in the queue, (otherwise it means an earlier list is currently running)
        pendingUpdates.singleOrNull()?.let {
            updateItemsInternal(it)
        }
    }

    private fun updateItemsInternal(nextList: List<ShortcutItemModel>) {
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

    private fun updateItemsUi(nextList: List<ShortcutItemModel>, diffResult: DiffUtil.DiffResult) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QsToggleItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemViewBinding = ListitemQsToggleBinding.inflate(inflater, parent, false)
        return QsToggleItemViewHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: QsToggleItemViewHolder, position: Int, payloads: MutableList<Any>) {
        payloads.filterIsInstance<String>().forEach { payload ->
            if (payload.isNotBlank() && payload == dataList[position].appItem?.packageName) {
                holder.updateAppIcon(dataList[position].appItem)
                return
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: QsToggleItemViewHolder, position: Int) {
        val itemData = dataList[position]
        holder.bind(itemData.appItem,
            { parentCallback?.onQsAssign(itemData.serviceTag) },
            { parentCallback?.onQsClear(itemData.serviceTag) },
            { parentCallback?.onQsItemClick() },
            itemData.appItem?.packageName?.let { { parentCallback?.onQsLongClick(it) } },
            itemData.appItem?.let { { parentCallback?.onAppIconClick(it) } }
        )
    }

    override fun getItemCount() = dataList.size

    class QsToggleItemViewHolder(private val itemViewBinding: ListitemQsToggleBinding) : RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bind(itemData: AppItemModel?, onAssign: () -> Unit, onClear: () -> Unit, onItemClick: () -> Unit, onLongClick: (() -> Unit)?, onAppIconClick: (() -> Unit)?) {
            itemViewBinding.apply {
                // Workaround to Android bug when using ellipses: https://issuetracker.google.com/issues/121092510
                txtAppname.setSingleLine()
                txtPackagename.setSingleLine()

                root.context?.let { _context ->
                    if (itemData != null) {
                        txtAppname.text = itemData.appName
                        txtPackagename.text = itemData.packageName

                        btnAssignApp.text = _context.getString(R.string.reassign)
                        root.setOnClickListener {
                            onItemClick.invoke()
                        }
                        root.setOnLongClickListener {
                            onLongClick?.invoke()
                            true
                        }

                        updateAppIcon(itemData)
                        imgAppicon.setOnClickListener {
                            onAppIconClick?.invoke()
                        }
                    } else {
                        txtAppname.text = _context.getString(R.string.unassigned_title, adapterPosition.inc())
                        txtPackagename.text = _context.getString(R.string.unassigned_subtitle)
                        imgAppicon.setImageResource(android.R.color.transparent)
                        btnAssignApp.text = _context.getString(R.string.assign)
                        root.isClickable = false
                        root.isLongClickable = false
                        imgAppicon.isClickable = false
                    }
                    btnAssignApp.setOnClickListener {
                        onAssign.invoke()
                    }
                    btnClearApp.setOnClickListener {
                        onClear.invoke()
                    }
                }
            }
        }

        fun updateAppIcon(itemData: AppItemModel?) {
            itemViewBinding.apply {
                itemData?.let {
                    itemData.appIcon?.let { icon ->
                        val monoIcon = Utils.convertMonoIcon(icon,
                            PrefHelper.getIconThreshold(itemData.packageName),
                            PrefHelper.getIconInvert(itemData.packageName)
                        )
                        imgAppicon.setImageBitmap(monoIcon)
                    } ?: run {
                        imgAppicon.setImageDrawable(itemData.appIcon)
                    }
                }
            }
        }
    }

    private class DiffUtilCallback(
        val oldList: List<ShortcutItemModel>,
        val newList: List<ShortcutItemModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].serviceTag == newList[newItemPosition].serviceTag
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition].appItem
            val newItem = newList[newItemPosition].appItem
            return oldItem?.packageName == newItem?.packageName
                    && oldItem?.appName == newItem?.appName
                    && oldItem?.appIcon?.hashCode() == newItem?.appIcon?.hashCode()
                    && oldItem?.isSystemApp == newItem?.isSystemApp
        }
    }
}