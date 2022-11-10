package com.csanders.commentarii.datamodel

data class Work(
    val header: WorkHeader,
    val text: List<Section2>
) {
}