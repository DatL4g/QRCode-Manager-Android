package de.datlag.qrcodemanager.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import de.datlag.qrcodemanager.R
import de.datlag.qrcodemanager.commons.applyAnimation
import de.datlag.qrcodemanager.fragments.childs.ContentFragment
import de.datlag.qrcodemanager.fragments.childs.NetworkContentFragment
import de.datlag.qrcodemanager.fragments.childs.TextContentFragment
import kotlinx.android.synthetic.main.fragment_create.*

class CreateFragment : Fragment() {

    private lateinit var textContentFragment: TextContentFragment
    private lateinit var networkContentFragment: NetworkContentFragment
    private var activeFragment: ContentFragment? = null
    private lateinit var saveContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        saveContext = context ?: activity ?: requireContext()

        textContentFragment = TextContentFragment.newInstance()
        networkContentFragment = NetworkContentFragment.newInstance()
        switchFragment(textContentFragment)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.textContent -> {
                    switchFragment(textContentFragment)
                    true
                }
                R.id.wifiContent -> {
                    switchFragment(networkContentFragment)
                    true
                }
                else -> false
            }
        }

        fabGenerate.setOnClickListener {
            activeFragment?.getContent().run {
                if (!this.isNullOrBlank() && !this.isNullOrEmpty()) {
                    val barCodeEncoder = BarcodeEncoder()
                    val bitmap = barCodeEncoder.encodeBitmap(activeFragment?.getContent(), BarcodeFormat.QR_CODE, 800, 800)
                    val imageView = ImageView(saveContext).apply { setImageBitmap(bitmap) }
                    MaterialAlertDialogBuilder(saveContext)
                        .setTitle("Generated QR Code")
                        .setView(imageView)
                        .setPositiveButton("Close", null)
                        .setNegativeButton("Save", null)
                        .create().applyAnimation().show()
                }
            }
        }

        fabScan.setOnClickListener {
            findNavController().navigate(R.id.action_CreateFragment_to_ScanFragment)
        }
    }

    private fun switchFragment(fragment: ContentFragment) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.createFragmentContainer, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
        activeFragment = fragment
    }
}
