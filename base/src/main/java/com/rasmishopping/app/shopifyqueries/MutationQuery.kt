package com.rasmishopping.app.shopifyqueries

import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.*
import com.shopify.graphql.support.ID

object MutationQuery {
    fun getLoginDetails(username: String, password: String): Storefront.MutationQuery {
        val input = Storefront.CustomerAccessTokenCreateInput(username, password)
        return Storefront.mutation { mutation ->
            mutation
                .customerAccessTokenCreate(
                    input
                ) { query ->
                    query
                        .customerAccessToken { customerAccessToken ->
                            customerAccessToken
                                .accessToken()
                                .expiresAt()
                        }
                        .customerUserErrors { cue -> cue.field().message() }

                }


        }
    }

    fun createaccount(firstname: String, lastname: String, email: String, password: String): Storefront.MutationQuery {
        val customer = Storefront.CustomerCreateInput(email, password)
        customer.firstName = firstname
        customer.lastName = lastname
        return Storefront.mutation { root ->
            root
                .customerCreate(
                    customer
                ) { customerquery ->
                    customerquery
                        .customer { customerdata ->
                            customerdata
                                .firstName()
                                .lastName()
                                .email()
                                .id()
                                .tags()
                        }
                        .customerUserErrors { cue -> cue.field().message() }
                }

        }
    }

    fun renewToken(token: String?): Storefront.MutationQuery {
        return Storefront.mutation { root ->
            root
                .customerAccessTokenRenew(
                    token
                ) { access ->
                    access
                        .customerAccessToken { r ->
                            r
                                .accessToken()
                                .expiresAt()
                        }
                        .userErrors { error ->
                            error
                                .message()
                                .field()
                        }
                }
        }
    }

    fun updateCustomer(
        input: Storefront.CustomerUpdateInput,
        token: String
    ): Storefront.MutationQuery {
        return Storefront.mutation { root ->
            root
                .customerUpdate(
                    token, input
                ) { customer ->
                    customer
                        .customer { c ->
                            c
                                .firstName()
                                .lastName()
                                .email()
                                .id()
                                .tags()
                        }
                        .customerAccessToken { access ->
                            access
                                .expiresAt()
                                .accessToken()
                        }
                        .customerUserErrors { cue -> cue.message().field() }
                }
        }
    }

    fun recoverCustomer(email: String): Storefront.MutationQuery {
        return Storefront.mutation { root ->
            root
                .customerRecover(
                    email
                ) { recover -> recover.customerUserErrors { cue -> cue.field().message() } }
        }
    }

    fun deleteCustomerAddress(token: String?, address_id: ID?): Storefront.MutationQuery {
        return Storefront.mutation { root ->
            root
                .customerAddressDelete(
                    address_id, token
                ) { customer ->
                    customer
                        .customerUserErrors { cue -> cue.field().message() }
                }
        }
    }

    fun addAddress(
        input: Storefront.MailingAddressInput?,
        token: String?
    ): Storefront.MutationQuery {
        return Storefront.mutation { root ->
            root.customerAddressCreate(
                token,
                input
            ) { address -> address.customerUserErrors { cue -> cue.field().message() } }
        }
    }

    fun updateAddress(
        input: Storefront.MailingAddressInput?,
        token: String?,
        address_id: ID?
    ): Storefront.MutationQuery {
        return Storefront.mutation { root ->
            root.customerAddressUpdate(token, address_id, input) { address ->
                address
                    .customerAddress { caddress ->
                        caddress.firstName().lastName().address1().address2().city().country()
                            .province().phone().zip()
                    }
                    .customerUserErrors { cue -> cue.field().message() }
            }
        }
    }

    fun populateShippingAddress(
        input: MailingAddressInput?,
        checkoutId: ID?
    ): Storefront.MutationQuery {
        return Storefront.mutation { root ->
            root.checkoutShippingAddressUpdateV2(
                input,
                checkoutId
            ) { usererrors -> usererrors
                .checkoutUserErrors { cue -> cue.code().field().message() }
                .checkout {s -> s.availableShippingRates { shiprate ->
                    shiprate.ready()
                        .shippingRates { h ->
                            h.handle().price { a -> a.amount() }.title()
                        }
                }
                .shippingAddress{ caddress ->
                    caddress.firstName().lastName().address1().country()
                        .province().zip().city().phone().company()
                }}}
        }
    }

    fun checkoutCompleteWithCreditCardV2(
        checkoutId: ID?,payment:CreditCardPaymentInputV2
    ): Storefront.MutationQuery {
        return Storefront.mutation { root ->
            root.checkoutCompleteWithCreditCardV2(
                checkoutId,payment
            ) { usererrors -> usererrors
                .checkoutUserErrors { cue -> cue.code().field().message() }
                .payment{id -> id.errorMessage()}
                }
        }
    }

    fun checkoutEmailUpdate(
        checkoutId: ID?,email: String
    ): Storefront.MutationQuery {
        return Storefront.mutation { root ->
            root.checkoutEmailUpdateV2(
                checkoutId,email
            ) { usererrors -> usererrors
                .checkoutUserErrors { cue -> cue.code().field().message() }
            }
        }
    }

    fun checkoutShippingLineUpdate(
        checkoutId: ID?, shippingRateHandle : String
    ): Storefront.MutationQuery {
        return Storefront.mutation { root ->
            root.checkoutShippingLineUpdate(
                checkoutId,shippingRateHandle
            )
            {
                    usererrors -> usererrors
                .checkoutUserErrors { cue -> cue.code().field().message()
                    }
                .checkout {
                }
            }
        }
    }

    fun checkoutCustomerAssociateV2(
        checkoutId: ID?,
        customerAccessToken: String?,
        directive: List<InContextDirective>? = null
    ): Storefront.MutationQuery {
        return mutation(directive) { mutation: Storefront.MutationQuery ->
            mutation
                .checkoutCustomerAssociateV2(
                    checkoutId, customerAccessToken
                ) { query: CheckoutCustomerAssociateV2PayloadQuery ->
                    query
                        .checkout { check -> check.webUrl() }
                        .checkoutUserErrors { userError->
                            userError
                                .field()
                                .message()
                        }
                }
        }
    }

    fun checkoutDiscountCodeApply(
        checkoutId: ID?,
        discount_code: String?,
        directive: List<InContextDirective>? = null
    ): Storefront.MutationQuery {
        return mutation(directive) { mutation: Storefront.MutationQuery ->
            mutation.checkoutDiscountCodeApplyV2(discount_code, checkoutId)
            { query: CheckoutDiscountCodeApplyV2PayloadQuery ->
                query
                    .checkout { check ->
                        check.webUrl()
                            .paymentDue({ pd -> pd.amount().currencyCode() })
                            .subtotalPrice({ st -> st.currencyCode().amount() })
                            .lineItemsSubtotalPrice({ st -> st.currencyCode().amount() })
                            .taxesIncluded()
                            .taxExempt()
                            .totalTax({ tt -> tt.amount().currencyCode() })
                            .totalPrice({ tp -> tp.currencyCode().amount() })
                            .discountApplications({ it.first(10) }) { it ->
                                it.edges { it ->
                                    it.node { it ->
                                        it.allocationMethod()
                                            .targetSelection()
                                            .targetType()
                                            .onAutomaticDiscountApplication({a->a.title()})
                                            .onManualDiscountApplication({a->a.title()})
                                            .onScriptDiscountApplication({a->a.title()})
                                            .onDiscountCodeApplication({a->a.code()})
                                            .value { it ->
                                                it.onMoneyV2 { it ->
                                                    it.amount()
                                                        .currencyCode()
                                                }
                                                it.onPricingPercentageValue { it ->
                                                    it.percentage()
                                                }
                                            }
                                    }

                                }
                                    .nodes { it ->
                                        it.allocationMethod()
                                            .targetSelection()
                                            .targetType()
                                            .onAutomaticDiscountApplication({a->a.title()})
                                            .onManualDiscountApplication({a->a.title()})
                                            .onScriptDiscountApplication({a->a.title()})
                                            .onDiscountCodeApplication({a->a.code()})
                                            .value { it ->
                                                it.onMoneyV2 { it ->
                                                    it.amount()
                                                        .currencyCode()
                                                }
                                                it.onPricingPercentageValue { it ->
                                                    it.percentage()
                                                }
                                            }}
                            }.lineItems({ it.first(50) }, { args ->
                                args.edges { egs ->
                                    egs.cursor()
                                        .node { linenode ->
                                            linenode
                                                .title()
                                                .quantity()
                                                .variant({ linevariant ->
                                                    linevariant
                                                        .title()
                                                        .product({ p -> p.title().onlineStoreUrl()})
                                                        .availableForSale()
                                                        .currentlyNotInStock()
                                                        .quantityAvailable()
                                                        .price({ p ->
                                                            p.currencyCode().amount()
                                                                .currencyCode()
                                                        })
                                                        .compareAtPrice({ c ->
                                                            c.amount().amount().currencyCode()
                                                        })
                                                        .image({ img ->
                                                            img.url().height().width()

                                                        }
                                                        )
                                                        .selectedOptions({ select ->
                                                            select
                                                                .name()
                                                                .value()

                                                        }
                                                        )

                                                }
                                                )
                                        }
                                }
                                    .nodes({a->a.title().variant({v->v.title()})})
                            })
                    }
                    .checkoutUserErrors { userError->
                        userError
                            .field()
                            .message()
                    }
            }

        }
    }

    fun checkoutDiscountCodeRemove(checkoutId: ID?): Storefront.MutationQuery {
        return mutation { mutation: Storefront.MutationQuery ->
            mutation.checkoutDiscountCodeRemove(checkoutId)
            { query: CheckoutDiscountCodeRemovePayloadQuery ->
                query
                    .checkout { check ->
                        check.webUrl()
                            .paymentDue({ pd -> pd.amount().currencyCode() })
                            .subtotalPrice({ st -> st.currencyCode().amount() })
                            .lineItemsSubtotalPrice({ st -> st.currencyCode().amount() })
                            .taxesIncluded()
                            .taxExempt()
                            .totalTax({ tt -> tt.amount().currencyCode() })
                            .totalPrice({ tp -> tp.currencyCode().amount() })
                    }
                    .checkoutUserErrors{ userError ->
                        userError
                            .field()
                            .message()
                    }
            }
        }
    }


    fun checkoutGiftCardsAppend(
        checkoutId: ID?,
        gift_card: List<String>?,
        directive: List<InContextDirective>? = null
    ): Storefront.MutationQuery {
        return mutation(directive) { mutation: Storefront.MutationQuery ->
            mutation.checkoutGiftCardsAppend(gift_card, checkoutId)
            { query: CheckoutGiftCardsAppendPayloadQuery ->
                query
                    .checkout { check ->
                        check.webUrl()
                            .paymentDue({ pd -> pd.amount().currencyCode() })
                            .subtotalPrice({ st -> st.currencyCode().amount() })
                            .lineItemsSubtotalPrice({ st -> st.currencyCode().amount() })
                            .taxesIncluded()
                            .taxExempt()
                            .totalTax({ tt -> tt.amount().currencyCode() })
                            .totalPrice({ tp -> tp.currencyCode().amount() })
                            .appliedGiftCards { gc ->
                                gc.amountUsed(
                                    { a ->
                                        a.amount()
                                            .currencyCode()
                                    }
                                )
                                    .lastCharacters()

                            }
                    }
                    .checkoutUserErrors { userError ->
                        userError
                            .field()
                            .message()
                    }
            }
        }
    }


    fun checkoutGiftCardsRemove(
        appliedGiftCardId: ID?,
        checkoutId: ID?,
        directive: List<InContextDirective>? = null
    ): Storefront.MutationQuery {
        return mutation(directive) { mutation: Storefront.MutationQuery ->
            mutation.checkoutGiftCardRemoveV2(appliedGiftCardId, checkoutId)
            { query: CheckoutGiftCardRemoveV2PayloadQuery ->
                query
                    .checkout { check ->
                        check.webUrl()
                            .paymentDue({ pd -> pd.amount().currencyCode() })
                            .subtotalPrice({ st -> st.currencyCode().amount() })
                            .taxesIncluded()
                            .taxExempt()
                            .totalTax({ tt -> tt.amount().currencyCode() })
                            .totalPrice({ tp -> tp.currencyCode().amount() })
                            .appliedGiftCards { gc ->
                                gc.amountUsed(
                                    { a ->
                                        a.amount()
                                            .currencyCode()
                                    }
                                )
                            }
                        
                    }
                    .checkoutUserErrors { userError->
                        userError
                            .field()
                            .message()
                    }
            }
        }
    }

    fun multipass(multipassToken: String?): Storefront.MutationQuery {

        return Storefront.mutation { root ->
            root.customerAccessTokenCreateWithMultipass(multipassToken)
            { query: CustomerAccessTokenCreateWithMultipassPayloadQuery ->
                query.customerAccessToken { c ->
                    c.accessToken()
                        .expiresAt()

                }
                    .customerUserErrors { e ->
                        e.message()
                            .field()
                    }

            }

        }
    }

}

