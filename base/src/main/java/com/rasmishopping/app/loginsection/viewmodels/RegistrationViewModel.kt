package com.rasmishopping.app.loginsection.viewmodels

import android.content.Context

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.dbconnection.entities.CustomerTokenData
import com.rasmishopping.app.dbconnection.entities.UserLocalData
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLMutateGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.shopifyqueries.MutationQuery
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.GraphQLResponse
import com.rasmishopping.app.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class RegistrationViewModel(private val repository: Repository) : ViewModel() {
    val message = MutableLiveData<String>()
    private val responsedata = MutableLiveData<Storefront.Customer>()
    private val loginresponsedata = MutableLiveData<Storefront.CustomerAccessToken>()
    private var password = ""
    lateinit var context: Context
    fun LoginResponse(): MutableLiveData<Storefront.CustomerAccessToken> {
        return loginresponsedata
    }

    fun Response(): MutableLiveData<Storefront.Customer> {
        return responsedata
    }

    fun getRegistrationDetails(
        firstname: String,
        lastname: String,
        email: String,
        password: String
    ) {
        this.password = password
        registeruseer(firstname, lastname, email, password)
    }

    private fun registeruseer(
        firstname: String,
        lastname: String,
        email: String,
        password: String
    ) {
        try {
            doGraphQLMutateGraph(
                repository,
                MutationQuery.createaccount(firstname, lastname, email, password),
                customResponse = object : CustomResponse {
                    override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                        invoke(result)
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private operator fun invoke(graphCallResult: GraphCallResult<Storefront.Mutation>): Unit {
        if (graphCallResult is GraphCallResult.Success<*>) {
            consumeResponse(GraphQLResponse.success(graphCallResult as GraphCallResult.Success<*>))
        } else {
            consumeResponse(GraphQLResponse.error(graphCallResult as GraphCallResult.Failure))
        }
        return Unit
    }

    private fun consumeResponse(reponse: GraphQLResponse) {
        Constant.logCompleteRegistrationEvent("shopiy", context)
        when (reponse.status) {
            Status.SUCCESS -> {
                val result = (reponse.data as GraphCallResult.Success<Storefront.Mutation>).response
                if (result.hasErrors) {
                    val errors = result.errors
                    val iterator = errors.iterator()
                    val errormessage = StringBuilder()
                    var error: Error? = null
                    while (iterator.hasNext()) {
                        error = iterator.next()
                        errormessage.append(error.message())
                    }
                    message.setValue(errormessage.toString())
                } else {
                    val errors = result.data!!.customerCreate.customerUserErrors
                    if (errors.size > 0) {
                        val iterator = errors.iterator()
                        var err = ""
                        while (iterator.hasNext()) {
                            val error = iterator.next() as Storefront.CustomerUserError
                            err += error.message
                        }
                        message.setValue(err)
                    } else {
                        responsedata.setValue(result.data!!.customerCreate.customer)
                    }
                }
            }
            Status.ERROR -> message.setValue(reponse.error!!.error.message)
            else -> {
            }
        }
    }

    fun insertUserData(customer: Storefront.Customer) {
        val userLocalData =
            UserLocalData(customer.firstName, customer.lastName, customer.email, password)
        MagePrefs.setCustomerId((customer.id.toString())!!)
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertUserData(userLocalData)
        }
        getLoginData(customer.email, password)
    }

    private fun getLoginData(email: String, password: String) {
        try {
            doGraphQLMutateGraph(
                repository,
                MutationQuery.getLoginDetails(email, password),
                customResponse = object : CustomResponse {
                    override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                        invokes(result)
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun invokes(graphCallResult: GraphCallResult<Storefront.Mutation>): Unit {
        if (graphCallResult is GraphCallResult.Success<*>) {
            consumeLoginResponse(GraphQLResponse.success(graphCallResult as GraphCallResult.Success<*>))
        } else {
            consumeLoginResponse(GraphQLResponse.error(graphCallResult as GraphCallResult.Failure))
        }
        return Unit
    }

    private fun consumeLoginResponse(graphQLResponse: GraphQLResponse) {
        when (graphQLResponse.status) {
            Status.SUCCESS -> {
                val result =
                    (graphQLResponse.data as GraphCallResult.Success<Storefront.Mutation>).response
                if (result.hasErrors) {
                    val errors = result.errors
                    val iterator = errors.iterator()
                    val errormessage = StringBuilder()
                    var error: Error? = null
                    while (iterator.hasNext()) {
                        error = iterator.next()
                        errormessage.append(error.message())
                    }
                    message.setValue(errormessage.toString())
                } else {
                    val errors = result.data!!.customerAccessTokenCreate.customerUserErrors
                    if (errors.size > 0) {
                        val iterator = errors.iterator()
                        var err = ""
                        while (iterator.hasNext()) {
                            val error = iterator.next() as Storefront.CustomerUserError
                            err += error.message
                        }
                        message.setValue(err)
                    } else {
                        loginresponsedata.setValue(result.data!!.customerAccessTokenCreate.customerAccessToken)
                    }
                }
            }
            Status.ERROR -> message.setValue(graphQLResponse.error!!.error.message)
            else -> {
            }
        }
    }

    fun savetoken(token: Storefront.CustomerAccessToken) {
        val customerTokenData =
            CustomerTokenData(token.accessToken, token.expiresAt.toLocalDateTime().toString())
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveaccesstoken(customerTokenData)
        }
    }

    fun isValidEmail(target: String): Boolean {
        val emailPattern = Pattern.compile(
            "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
            Pattern.CASE_INSENSITIVE
        )
        return emailPattern.matcher(target).matches()
    }
    fun isStrongPassword(target: String): Boolean{
        val passPattern = Pattern.compile(
            "[^(?=.[0-9])(?=.[a-z])(?=.[A-Z])(?=.[@#$%^&+=])(?=\\S+$).{4,}$]"

        )
        return passPattern.matcher(target).matches()
    }
}
