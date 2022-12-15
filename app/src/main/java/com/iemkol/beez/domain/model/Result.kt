package com.iemkol.beez.domain.model

data class Result(
    val entities: List<Entity>,
    val md5: String,
    val name: String,
    val status: Status
)