package de.datlag.qrcodemanager

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkSpecifier
import android.net.wifi.*
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.net.ConnectivityManagerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import de.datlag.qrcodemanager.commons.*
import de.datlag.qrcodemanager.util.NetworkManager
import de.datlag.qrcodemanager.util.ScanManager
import kotlinx.android.synthetic.main.fragment_first.*
import org.json.JSONObject
import java.util.*


class ScanFragment : Fragment(), PermissionListener {

    private lateinit var saveContext: Context
    private lateinit var fileScanner: FileScanner
    private lateinit var scanManager: Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        saveContext = context ?: activity ?: requireContext()
        fileScanner = FileScanner(saveContext)
        scanManager = ScanManager(saveContext)

        Dexter.withContext(saveContext)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(this).check()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabOpen.setOnClickListener {
            Dexter.withContext(saveContext)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(this@ScanFragment).check()
        }

        fabCreate.setOnClickListener {
            findNavController().navigate(R.id.action_ScanFragment_to_CreateFragment)
        }

        barCodeScanner.viewFinder.setLaserVisibility(false)
        barCodeScanner.viewFinder.setMaskColor(Color.parseColor("#99000000"))
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
                ).apply { type = "image/*" }

                startActivityForResult(Intent.createChooser(pickIntent, "Select Image"), GALLERY_REQUEST_CODE)
            }
        }
    }

    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
        when(p0?.name) {
            Manifest.permission.CAMERA -> {
                MaterialAlertDialogBuilder(saveContext)
                    .setTitle("Scan Environment")
                    .setMessage("To scan codes in your environment you have to grant access to your camera")
                    .setPositiveButton("Grant"){ _, _ -> p1?.continuePermissionRequest() }
                    .setNegativeButton("Cancel"){ _, _ -> p1?.cancelPermissionRequest() }
                    .setOnDismissListener {  }
                    .create().applyAnimation().show()
            }
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                MaterialAlertDialogBuilder(saveContext)
                    .setTitle("Scan Pictures")
                    .setMessage("To scan pictures you have to grant access to your files")
                    .setPositiveButton("Grant"){ _, _ -> p1?.continuePermissionRequest() }
                    .setNegativeButton("Cancel"){ _, _ -> p1?.cancelPermissionRequest() }
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
                    fileScanner.scanFromUri(data.data!!, object: FileScanner.Callback{
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

        fun newInstance(): ScanFragment {
            return ScanFragment()
        }
    }
}
