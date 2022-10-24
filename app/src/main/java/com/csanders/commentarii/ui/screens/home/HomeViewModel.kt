package com.csanders.commentarii.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csanders.commentarii.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    fun getKenJennings(): Int {
        return R.drawable.ken_jennings_forehead
    }

    companion object {
        const val route = "home"
        const val semanticName = "home screen"
    }
}