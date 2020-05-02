package de.datlag.qrcodemanager

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import de.datlag.qrcodemanager.commons.applyStatusBarColor
import de.datlag.qrcodemanager.commons.enterFullScreen
import de.datlag.qrcodemanager.commons.hideSystemUI
import de.datlag.qrcodemanager.commons.useNotchSpace

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.enterFullScreen()
        window.hideSystemUI()
        window.useNotchSpace()
        window.applyStatusBarColor(ContextCompat.getColor(this, R.color.statusBarColor))
    }
}
