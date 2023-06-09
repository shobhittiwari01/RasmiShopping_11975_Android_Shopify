package com.rasmishopping.app.userprofilesection.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.R
import com.rasmishopping.app.dbconnection.entities.CustomerTokenData
import com.rasmishopping.app.dbconnection.entities.UserLocalData
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLMutateGraph
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.shopifyqueries.MutationQuery
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.userprofilesection.models.User
import com.rasmishopping.app.utils.GraphQLResponse
import com.rasmishopping.app.utils.Status

class UserProfileViewModel(private val repository: Repository) : ViewModel() {
    private var data: CustomerTokenData? = null
    private var user: UserLocalData? = null
    private val response = MutableLiveData<Storefront.Customer>()
    private val password = MutableLiveData<String>()
    private val profileupdateflag = MutableLiveData<Boolean>()
    lateinit var context: Context
    val errorMessageResponse = MutableLiveData<String>()
    val passwordResponse: MutableLiveData<String>
        get() {
            fetchPassword()
            return password
        }

    fun getResponse_(): MutableLiveData<Storefront.Customer> {
        fetchData()
        return response
    }

    fun getFlag(): MutableLiveData<Boolean> {
        return profileupdateflag
    }

    private fun fetchPassword() {
        try {
            val runnable = Runnable {
                user = repository.allUserData[0]
                password.postValue(user!!.password)
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchData() {
        try {
            data = repository.accessToken[0]
            doGraphQLQueryGraph(
                repository,
                Query.getCustomerDetails(data!!.customerAccessToken!!),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invoke(result)
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private operator fun invoke(graphCallResult: GraphCallResult<Storefront.QueryRoot>): Unit {
        if (graphCallResult is GraphCallResult.Success<*>) {
            consumeDataResponse(GraphQLResponse.success(graphCallResult as GraphCallResult.Success<*>))
        } else {
            consumeDataResponse(GraphQLResponse.error(graphCallResult as GraphCallResult.Failure))
        }
        return Unit
    }

    private fun consumeDataResponse(graphQLResponse: GraphQLResponse) {
        when (graphQLResponse.status) {
            Status.SUCCESS -> {
                val result =
                    (graphQLResponse.data as GraphCallResult.Success<Storefront.QueryRoot>).response
                if (result.hasErrors) {
                    val errors = result.errors
                    val iterator = errors.iterator()
                    val errormessage = StringBuilder()
                    var error: Error? = null
                    while (iterator.hasNext()) {
                        error = iterator.next()
                        errormessage.append(error.message())
                    }
                    errorMessageResponse.setValue(errormessage.toString())
                } else {
                    try {
                        response.setValue(result.data?.customer)
                    } catch (e: Exception) {
                        response.setValue(result.data?.customer)
                    }
                }
            }
            Status.ERROR -> errorMessageResponse.setValue(graphQLResponse.error!!.error.message)
            else -> {
            }
        }
    }

    fun saveUser(localuser: User) {
        try {
            val runnable = Runnable {
                user!!.firstname = localuser.firstname
                user!!.lastname = localuser.lastname
                user!!.email = localuser.email
                repository.updateUserData(user!!)
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun updateDataonServer(localuser: User) {
        val update = Storefront.CustomerUpdateInput()
        update.email = localuser.email
        update.firstName = localuser.firstname
        update.lastName = localuser.lastname
        update.password = localuser.password
        user!!.password = localuser.password
        try {
            doGraphQLMutateGraph(
                repository,
                MutationQuery.updateCustomer(update, data!!.customerAccessToken!!),
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
            consumeResponse(GraphQLResponse.success(graphCallResult as GraphCallResult.Success<*>))
        } else {
            consumeResponse(GraphQLResponse.error(graphCallResult as GraphCallResult.Failure))
        }
        return Unit
    }

    private fun consumeResponse(reponse: GraphQLResponse) {
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
                    errorMessageResponse.setValue(errormessage.toString())
                } else {
                    val errors = result.data!!.customerUpdate.customerUserErrors
                    if (errors.size > 0) {
                        val iterator = errors.iterator()
                        var err = ""
                        while (iterator.hasNext()) {
                            val error = iterator.next() as Storefront.CustomerUserError
                            err += error.message
                        }
                        errorMessageResponse.setValue(err)
                    } else {
                        try {
                            val customer = result.data!!.customerUpdate.customer
                            val user = User()
                            user.firstname = customer.firstName
                            user.lastname = customer.lastName
                            user.email = customer.email
                            saveUser(user)
                            saveCustomerToken(result.data!!.customerUpdate.customerAccessToken)
                            profileupdateflag.value = true
                            errorMessageResponse.setValue(context.resources.getString(R.string.update_msg))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            Status.ERROR -> errorMessageResponse.setValue(reponse.error!!.error.message)
            else -> {
            }
        }
    }

    private fun saveCustomerToken(token: Storefront.CustomerAccessToken) {
        try {
            val runnable = Runnable {
                data!!.customerAccessToken = token.accessToken
                data!!.expireTime = token.expiresAt.toString()
                repository.updateAccessToken(data!!)
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun isValidEmail(target: String): Boolean {
        var valid = false
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        if (target.matches(emailPattern.toRegex())) {
            valid = true
        }
        return valid
    }

    fun logOut() {
        val runnable = Runnable {
            Log.i("MageNative", "LeftMenuResume 5")
            repository.deleteLocalData()
            repository.deletecart()
            repository.deleteWishListData()
            repository.deleteUserData()
            MagePrefs.clearRecent()
            //fetchUserData()
        }
        Thread(runnable).start()
    }
}
