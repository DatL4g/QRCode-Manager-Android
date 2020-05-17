package de.datlag.qrcodemanager.fragments.childs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.datlag.qrcodemanager.R

class NetworkContentFragment : ContentFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_network_content, container, false)
    }

    override fun getContent(): String {
        return ""
    }

    companion object {
        fun newInstance() = NetworkContentFragment()
    }

}