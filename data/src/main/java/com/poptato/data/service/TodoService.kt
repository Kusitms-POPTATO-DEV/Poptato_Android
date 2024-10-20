package com.poptato.data.service

import com.poptato.data.base.ApiResponse
import com.poptato.data.base.Endpoints
import com.poptato.domain.model.request.todo.TodoIdModel
import com.poptato.domain.model.request.todo.DeadlineContentModel
import com.poptato.domain.model.request.todo.DragDropRequestModel
import com.poptato.domain.model.request.todo.TodoContentModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.Path

interface TodoService {
    @DELETE(Endpoints.Todo.DELETE)
    suspend fun deleteTodo(
        @Path("todoId") todoId: Long
    ): Response<ApiResponse<Unit>>

    @PATCH(Endpoints.Todo.MODIFY)
    suspend fun modifyTodo(
        @Path("todoId") todoId: Long,
        @Body request: TodoContentModel
    ): Response<ApiResponse<Unit>>

    @PATCH(Endpoints.Todo.DRAG_DROP)
    suspend fun dragDrop(
        @Body request: DragDropRequestModel
    ): Response<ApiResponse<Unit>>

    @PATCH(Endpoints.Todo.DEADLINE)
    suspend fun updateDeadline(
        @Path("todoId") todoId: Long,
        @Body request: DeadlineContentModel
    ): Response<ApiResponse<Unit>>

    @PATCH(Endpoints.Todo.BOOKMARK)
    suspend fun updateBookmark(
        @Path("todoId") todoId: Long
    ): Response<ApiResponse<Unit>>

    @PATCH(Endpoints.Todo.SWIPE)
    suspend fun swipeTodo(
        @Body request: TodoIdModel
    ): Response<ApiResponse<Unit>>

    @PATCH(Endpoints.Todo.COMPLETION)
    suspend fun updateTodoCompletion(
        @Path("todoId") todoId: Long
    ): Response<ApiResponse<Unit>>
}