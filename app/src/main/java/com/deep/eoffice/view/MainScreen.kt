package com.deep.eoffice.view

import com.deep.dpwork.annotation.DpStatus
import com.deep.dpwork.core.kotlin.BaseScreenKt
import com.deep.eoffice.data.ColChild
import com.deep.eoffice.data.RowChild
import com.deep.eoffice.databinding.MainScreenLayoutBinding

@DpStatus(blackFont = true)
class MainScreen : BaseScreenKt<MainScreenLayoutBinding>() {

    private val excelData: MutableList<ColChild> = ArrayList()

    private fun initData() {
        // 列
        for (i in 1..12 step 1) {
            val c = ColChild()
            // 行
            for (k in 1..30 step 1) {
                val r = RowChild()
                // 数据
                r.childData = "${k * i}"
                c.rowChild.add(r)
            }
            excelData.add(c)
        }
    }

    override fun mainInit() {

        initData()

        here.excelView.setExcelData(excelData)
    }

    override fun onBack() {

    }
}