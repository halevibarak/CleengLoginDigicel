package com.applicaster.cleeng.network

import com.applicaster.cleeng.network.error.WebServiceError
import com.applicaster.cleeng.network.interceptor.PublisherInfoInterceptor
import com.applicaster.cleeng.network.request.RegisterRequestData
import com.applicaster.cleeng.network.request.RestoreSubscriptionsRequestData
import com.applicaster.cleeng.network.request.SubscribeRequestData
import com.applicaster.cleeng.network.request.SubscriptionsRequestData
import com.applicaster.cleeng.network.response.AuthResponseData
import com.applicaster.cleeng.network.response.ResetPasswordResponseData
import com.applicaster.cleeng.network.response.RestoreSubscriptionsResponseData
import com.applicaster.cleeng.network.response.SubscriptionsResponseData

class NetworkHelper {

    private val retrofitService by lazy {
        ServiceGenerator.setCustomInterceptor(PublisherInfoInterceptor())
        ServiceGenerator.createRetrofitService(RestService::class.java)
    }

    suspend fun register(requestData: RegisterRequestData): Result<List<AuthResponseData>, WebServiceError> {
        return try {
            val response = with(requestData) {
                retrofitService.registerEmail(
                    email,
                    password.orEmpty(),
                    country,
                    locale,
                    currency
                )
            }
            handleResponse(response)
        } catch (t: Throwable) {
            Result.Failure(WebServiceError.DEFAULT)
        }
    }

    suspend fun registerFacebook(requestData: RegisterRequestData): Result<List<AuthResponseData>, WebServiceError> {
        return try {
            val response = with(requestData) {
                retrofitService.registerFacebook(
                    email,
                    facebookId.orEmpty(),
                    country,
                    locale,
                    currency
                )
            }
            handleResponse(response)
        } catch (t: Throwable) {
            Result.Failure(WebServiceError.DEFAULT)
        }
    }

    suspend fun login(email: String, password: String): Result<List<AuthResponseData>, WebServiceError> {
        return try {
            val response = retrofitService.loginEmail(email, password)
            handleResponse(response)
        } catch (t: Throwable) {
            Result.Failure(WebServiceError.DEFAULT)
        }
    }

    suspend fun loginFacebook(email: String, facebookId: String): Result<List<AuthResponseData>, WebServiceError> {
        return try {
            val response = retrofitService.loginFacebook(email, facebookId)
            handleResponse(response)
        } catch (t: Throwable) {
            Result.Failure(WebServiceError.DEFAULT)
        }
    }

    suspend fun resetPassword(email: String): Result<ResetPasswordResponseData, WebServiceError> {
        return try {
            val response = retrofitService.resetPassword(email)
            handleResponse(response)
        } catch (t: Throwable) {
            Result.Failure(WebServiceError.DEFAULT)
        }
    }

    suspend fun extendToken(token: String): Result<List<AuthResponseData>, WebServiceError> {
        return try {
            val response = retrofitService.extendToken(token)
            handleResponse(response)
        } catch (t: Throwable) {
            Result.Failure(WebServiceError.DEFAULT)
        }
    }

    suspend fun requestSubscriptions(requestData: SubscriptionsRequestData): Result<List<SubscriptionsResponseData>, WebServiceError> {
        return try {
            val response = retrofitService.requestSubscriptions(requestData)
            handleResponse(response)
        } catch (t: Throwable) {
            Result.Failure(WebServiceError.DEFAULT)
        }
    }

    suspend fun subscribe(requestData: SubscribeRequestData): Result<Unit, WebServiceError> {
        return try {
            val response = retrofitService.subscribe(requestData)
            handleResponse(response)
        } catch (t: Throwable) {
            Result.Failure(WebServiceError.DEFAULT)
        }
    }

    suspend fun restoreSubscriptions(requestData: RestoreSubscriptionsRequestData):
            Result<List<RestoreSubscriptionsResponseData>, WebServiceError> {
        return try {
            val response = retrofitService.restoreSubscriptions(requestData)
            handleResponse(response)
        } catch (t: Throwable) {
            Result.Failure(WebServiceError.DEFAULT)
        }
    }

    suspend fun generateCustomerToken(email: String):
            Result<List<AuthResponseData>, WebServiceError> {
        return try {
            val response = retrofitService.generateCustomerToken(email)
            handleResponse(response)
        } catch (t: Throwable) {
            Result.Failure(WebServiceError.DEFAULT)
        }
    }
}