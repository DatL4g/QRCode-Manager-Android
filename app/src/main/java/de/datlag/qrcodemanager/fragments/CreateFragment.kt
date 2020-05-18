package de.datlag.qrcodemanager.fragments

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.EnvironmentCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import de.datlag.qrcodemanager.R
import de.datlag.qrcodemanager.commons.applyAnimation
import de.datlag.qrcodemanager.fragments.childs.ContentFragment
import de.datlag.qrcodemanager.fragments.childs.NetworkContentFragment
import de.datlag.qrcodemanager.fragments.childs.TextContentFragment
import kotlinx.android.synthetic.main.fragment_create.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.util.*

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
                        .setTitle(saveContext.getString(R.string.create_generated))
                        .setView(imageView)
                        .setPositiveButton(saveContext.getString(R.string.close), null)
                        .setNegativeButton(saveContext.getString(R.string.save)) { _, _ ->
                            try {
                                saveImage(bitmap, saveContext.getString(R.string.create_qr_prefix) + Calendar.getInstance().timeInMillis)
                            } catch (ignored: Exception) { }
                        }
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

    @Throws(IOException::class)
    @Suppress("DEPRECATION")
    private fun saveImage(bitmap: Bitmap, name: String): Boolean {
        var fileOutputStream: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = saveContext.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name + saveContext.getString(R.string.create_jpg_suffix))
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, saveContext.getString(R.string.create_jpg_mime))
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + saveContext.getString(R.string.create_images_folder))
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: Uri.EMPTY
            fileOutputStream = resolver.openOutputStream(imageUri)
        } else {
            val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + saveContext.getString(R.string.create_images_folder)
            val file = File(imageDir)

            if (!file.exists()) {
                file.mkdir()
            }

            val image = File(imageDir, name + saveContext.getString(R.string.create_jpg_suffix))
            fileOutputStream = FileOutputStream(image)
        }

        val saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fileOutputStream)
        fileOutputStream?.flush()
        fileOutputStream?.close()

        return saved
    }

    companion object {
        fun newInstance() = CreateFragment()
    }
}
