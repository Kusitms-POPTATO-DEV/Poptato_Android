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

    object Todo {
        private const val TODO = "/todo"
        const val DELETE = "$TODO/{todoId}"
        const val MODIFY = "$DELETE/content"
        const val DRAG_DROP = "/dragAndDrop"
    }
}