package com.poptato.data.base

object Endpoints {

    object Auth {
        private const val AUTH = "/auth"
        const val LOGIN = "$AUTH/login"
        const val REFRESH = "$AUTH/refresh"
    }

    object Backlog {
        const val BACKLOG = "/backlog"
        const val BACKLOGS = "/backlogs"
    }

    object MyPage {
        const val USER_DELETE = "/user"
    }
}