package de.datlag.qrcodemanager.fragments.childs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.datlag.qrcodemanager.R
import kotlinx.android.synthetic.main.fragment_text_content.*

class TextContentFragment : ContentFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_text_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getContent(): String {
        return textEditText?.text?.toString() ?: String()
    }

    companion object {
        fun newInstance() = TextContentFragment()
    }

}