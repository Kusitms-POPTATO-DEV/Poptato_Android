package com.poptato.domain.base

class ApiException(
    val code: Int,
    message: String
): Exception(message)