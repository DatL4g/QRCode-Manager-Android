package de.datlag.qrcodemanager.fragments.childs

import androidx.fragment.app.Fragment

abstract class ContentFragment : Fragment() {

    abstract fun getContent(): String

}