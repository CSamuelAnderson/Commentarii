package com.csanders.commentarii.ui.screens.textreader

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.csanders.commentarii.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TextReaderViewModel @Inject constructor() : ViewModel() {
    val semanticName = "text reader screen"

    fun getText(): String {
        return "substant. Intellectus enim uniuersalium rerum ex particularibus sumptus est. Quocirca cum ipsae subsistentiae in uniuersalibus quidem sint,in particularibus uero capiant substantiam, iure subsistentias particulariter substantes\n"
    }

    companion object {
        const val route = "text_reader"
    }
}