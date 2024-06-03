package com.ftg.carrepo.Utils

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import com.ftg.carrepo.R

class LoadingDialog(
    activity: Activity,
) {
    private var dialog: AlertDialog
    init {
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun startLoading() {
        dialog.show()
    }

    fun stopLoading() {
        dialog.dismiss()
    }
}