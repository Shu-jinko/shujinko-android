package com.shujinko.app.viewmodel

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shujinko.app.data.UsageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsageStatsViewModel : ViewModel() {

    private val _usageList = MutableStateFlow<List<UsageItem>>(emptyList())
    val usageList: StateFlow<List<UsageItem>> = _usageList

    fun hasUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun loadUsageStats(context: Context) {
        viewModelScope.launch {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 1000 * 60 * 60 * 24 // 24시간

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            val pm = context.packageManager
            val usageItems = stats
                .filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }
                .map {
                    val label = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(it.packageName, 0)).toString()
                    } catch (e: PackageManager.NameNotFoundException) {
                        it.packageName
                    }
                    UsageItem(label, it.totalTimeInForeground / 1000L) // 초
                }

            _usageList.value = usageItems
        }
    }
}