package de.datlag.qrcodemanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import de.datlag.qrcodemanager.commons.*

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.applyStatusBarColor(ContextCompat.getColor(this, R.color.statusBarColor))
    }
}
