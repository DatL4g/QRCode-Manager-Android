package de.datlag.qrcodemanager.fragments.childs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import de.datlag.qrcodemanager.R
import kotlinx.android.synthetic.main.fragment_network_content.*

class NetworkContentFragment : ContentFragment() {

    private lateinit var saveContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        saveContext = context ?: activity ?: requireContext()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_network_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ArrayAdapter.createFromResource(
            saveContext,
            R.array.security_spinner_array,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            securitySpinner.adapter = it
        }

        securitySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                passwordEditText.isEnabled = position != 0
            }

        }
    }

    override fun getContent(): String {
        val security = when (securitySpinner.selectedItemPosition) {
            0 -> "nopass"
            1 -> "WEP"
            else -> "WPA"
        }
        return "WIFI:T:$security;S:${ssidEditText.text.toString()};P:${passwordEditText.text.toString()};H:${hiddenCheckbox.isChecked};"
    }

    companion object {
        fun newInstance() = NetworkContentFragment()
    }

}