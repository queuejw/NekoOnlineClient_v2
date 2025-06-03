package ru.neko.online.client.components.utils

import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheet(val context: Context) {

    private fun createInfoBottomSheet(layoutId: Int): BottomSheetDialog {
        val bottomSheet = BottomSheetDialog(context)
        bottomSheet.setContentView(layoutId)
        bottomSheet.dismissWithAnimation = true
        val view =
            bottomSheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(view!!)
        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true)
        return bottomSheet
    }

    fun getInfoBottomSheet(layoutId: Int): BottomSheetDialog {
        return createInfoBottomSheet(layoutId)
    }
}