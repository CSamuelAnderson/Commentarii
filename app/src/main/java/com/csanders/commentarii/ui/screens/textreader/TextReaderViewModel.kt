package com.csanders.commentarii.ui.screens.textreader

import androidx.lifecycle.ViewModel
import com.csanders.commentarii.R
import com.csanders.commentarii.utilities.TEIParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TextReaderViewModel @Inject constructor() : ViewModel() {
    val semanticName = "text reader screen"

    fun getText(): String {
        val work = TEIParser.parseFromResource(R.raw.apuleius_golden_ass_lat)
        return "blah"
    }

    companion object {
        const val route = "text_reader"
    }
}