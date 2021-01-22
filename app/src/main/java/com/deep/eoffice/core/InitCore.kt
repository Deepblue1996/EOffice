package com.deep.eoffice.core

import android.Manifest
import android.view.WindowManager
import com.deep.dpwork.annotation.DpMainScreenKt
import com.deep.dpwork.annotation.DpPermission
import com.deep.dpwork.core.kotlin.DpInitCoreKt
import com.deep.eoffice.view.MainScreen

/**
 * Class - 框架入口
 *
 * Created by Deepblue on 2018/9/29 0029.
 */
@DpPermission(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.INTERNET,
    Manifest.permission.ACCESS_NETWORK_STATE
)
@DpMainScreenKt(MainScreen::class)
class InitCore : DpInitCoreKt() {
    override fun mainInit() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}