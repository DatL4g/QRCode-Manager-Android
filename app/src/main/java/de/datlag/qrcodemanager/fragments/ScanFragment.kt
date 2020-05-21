package de.datlag.qrcodemanager.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import de.datlag.qrcodemanager.R
import de.datlag.qrcodemanager.commons.applyAnimation
import de.datlag.qrcodemanager.util.FileScanner
import de.datlag.qrcodemanager.util.ScanManager
import de.datlag.qrcodemanager.util.showInstall
import kotlinx.android.synthetic.main.fragment_scan.*


class ScanFragment : Fragment(), PermissionListener {

    private lateinit var saveContext: Context
    private lateinit var fileScanner: FileScanner
    private lateinit var scanManager: Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        saveContext = context ?: activity ?: requireContext()
        fileScanner = FileScanner(saveContext)
        scanManager = ScanManager(requireActivity())

        MobileAds.initialize(saveContext) { }

        Dexter.withContext(saveContext)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(this).check()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adView.loadAd(AdRequest.Builder().build())

        fabOpen.setOnClickListener {
            if (!requireActivity().showInstall()) {
                Dexter.withContext(saveContext)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(this@ScanFragment).check()
            }
        }

        fabCreate.setOnClickListener {
            findNavController().navigate(R.id.action_ScanFragment_to_CreateFragment)
        }

        barCodeScanner.viewFinder.setLaserVisibility(false)
        barCodeScanner.viewFinder.setMaskColor(ContextCompat.getColor(saveContext, R.color.scanMaskColor))
        barCodeScanner.statusView.text = String()
        barCodeScanner.statusView.visibility = View.GONE
        barCodeScanner.viewFinder.drawViewfinder()
        barCodeScanner.decodeContinuous { result ->
            if (result != null && result.text != null) {
                scanManager.onScanned(result.result)
            } else {
                scanManager.onFailure(null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        barCodeScanner.resume()
    }

    override fun onStop() {
        super.onStop()
        barCodeScanner.pause()
    }

    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
        when(p0?.permissionName) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                val pickIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ).apply { type = saveContext.getString(R.string.intent_image_type) }

                startActivityForResult(Intent.createChooser(pickIntent, saveContext.getString(R.string.intent_image_title)),
                    GALLERY_REQUEST_CODE
                )
            }
        }
    }

    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
        when(p0?.name) {
            Manifest.permission.CAMERA -> {
                MaterialAlertDialogBuilder(saveContext)
                    .setTitle(saveContext.getString(R.string.permission_camera_title))
                    .setMessage(saveContext.getString(R.string.permission_camera_message))
                    .setPositiveButton(saveContext.getString(R.string.grant)){ _, _ -> p1?.continuePermissionRequest() }
                    .setNegativeButton(saveContext.getString(R.string.cancel)){ _, _ -> p1?.cancelPermissionRequest() }
                    .setOnDismissListener {  }
                    .create().applyAnimation().show()
            }
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                MaterialAlertDialogBuilder(saveContext)
                    .setTitle(saveContext.getString(R.string.permission_storage_read_title))
                    .setMessage(saveContext.getString(R.string.permission_storage_read_message))
                    .setPositiveButton(saveContext.getString(R.string.grant)){ _, _ -> p1?.continuePermissionRequest() }
                    .setNegativeButton(saveContext.getString(R.string.cancel)){ _, _ -> p1?.cancelPermissionRequest() }
                    .create().applyAnimation().show()
            }
        }
    }

    override fun onPermissionDenied(p0: PermissionDeniedResponse?) { }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (data?.data != null) {
                    fileScanner.scanFromUri(data.data!!, object:
                        FileScanner.Callback {
                        override fun onResult(result: Result) {
                            scanManager.onScanned(result)
                        }

                        override fun onError(exception: Exception?) {
                            scanManager.onFailure(exception)
                        }
                    })
                }
            }
        }
    }

    interface Callback {
        fun onScanned(result: Result)
        fun onFailure(exception: Exception?)
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 420

        fun newInstance() = ScanFragment()
    }
}
