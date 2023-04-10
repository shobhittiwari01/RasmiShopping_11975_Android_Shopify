package com.rasmishopping.app.shopifyqueries

import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import com.rasmishopping.app.utils.Constant

object Mutation {
    fun createCheckout(
        inputs: Storefront.CheckoutCreateInput,
        directive: List<Storefront.InContextDirective>? = null
    ,viewType:String): Storefront.MutationQuery {
        var createcheckoutmutation: Storefront.MutationQuery? = null
        createcheckoutmutation = Storefront.mutation(directive) { root ->
            root
                .checkoutCreate(
                    inputs
                ) { checkoutquery ->
                    checkoutquery
                        .checkout { check ->
                            check
                                .webUrl()
                                .paymentDue({ pd -> pd.amount().currencyCode() })
                                .subtotalPrice({ st -> st.currencyCode().amount() })
                                .lineItemsSubtotalPrice({ st -> st.currencyCode().amount() })
                                .taxesIncluded()
                                .taxExempt()
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
                                }
                                .totalDuties({td->td.currencyCode().amount()})
                                .totalTax({ tt -> tt.amount().currencyCode() })
                                .totalPrice({ tp -> tp.currencyCode().amount() })
                                .lineItems({ it.first(50).reverse(false) }, { args ->
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
                                                                img.url({t->t.transform(Constant.imageConfiguration(viewType))}).height().width()

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
                        .queueToken()
                        .checkoutUserErrors { checkerror -> checkerror.field().message().code() }
                }
        }
        return createcheckoutmutation
    }

    fun updateCheckout(
    inputs: Storefront.CheckoutAttributesUpdateV2Input,
    directive: List<Storefront.InContextDirective>? = null
    ,viewType:String, checkoutID: ID): Storefront.MutationQuery {
            var createcheckoutmutation: Storefront.MutationQuery? = null
            createcheckoutmutation = Storefront.mutation(directive) { root ->
                root
                    .checkoutAttributesUpdateV2(checkoutID,
                        inputs
                    ) { checkoutquery ->
                        checkoutquery
                            .checkout { check ->
                                check
                                    .webUrl()
                                    .paymentDue({ pd -> pd.amount().currencyCode() })
                                    .subtotalPrice({ st -> st.currencyCode().amount() })
                                    .lineItemsSubtotalPrice({ st -> st.currencyCode().amount() })
                                    .taxesIncluded()
                                    .taxExempt()
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
                                    }
                                    .totalDuties({td->td.currencyCode().amount()})
                                    .totalTax({ tt -> tt.amount().currencyCode() })
                                    .totalPrice({ tp -> tp.currencyCode().amount() })
                                    .lineItems({ it.first(50).reverse(false) }, { args ->
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
                                                                    img.url({t->t.transform(Constant.imageConfiguration(viewType))}).height().width()

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
                            .checkoutUserErrors { checkerror -> checkerror.field().message().code() }
                    }
            }
            return createcheckoutmutation
    }

    fun additems(
        inputs: List<Storefront.CheckoutLineItemInput>,
        directive: List<Storefront.InContextDirective>,
        checkoutID: ID
    ): Storefront.MutationQuery {
        return Storefront.mutation(directive) {it->it
            .checkoutLineItemsAdd(inputs,checkoutID,{c->c.checkout({d->d.webUrl().totalPrice { t->t.amount().currencyCode() }})})
        }
    }
    fun cartCreation(
        inputs: Storefront.CartInput,
        directive: List<Storefront.InContextDirective>
    ): Storefront.MutationQuery {
        return Storefront.mutation(directive) {
            it.cartCreate({ it.input(inputs) }) {
                it.userErrors {
                    it.message()
                }
                it.cart() {
                    it.discountCodes {
                        it.applicable().code()
                    }
                    it.discountAllocations { it ->
                        it.discountedAmount { it ->
                            it.amount().currencyCode()

                        }
                        it.onCartCodeDiscountAllocation { it ->
                            it.code().discountedAmount { it ->
                                it.amount().currencyCode()
                            }

                        }
                        it.onCartAutomaticDiscountAllocation { it ->
                            it.discountedAmount { it ->
                                it.amount().currencyCode()
                            }
                        }
                        it.onCartCustomDiscountAllocation() { it ->
                            it.discountedAmount { it ->
                                it.amount().currencyCode()
                            }
                        }
                    }
                    it.checkoutUrl()
                    it.attributes { a->a.key().value() }
                    it.cost {
                        it.subtotalAmount {
                            it.amount().currencyCode()
                        }.totalAmount {
                            it.currencyCode().amount()
                        }
                            .totalTaxAmount {
                                it.currencyCode().currencyCode()
                            }
                            .totalTaxAmount {
                                it.currencyCode().amount()
                            }
                    }
                    it.deliveryGroups({ it.first(10) }) {it.edges{it.node { it.deliveryAddress { it.firstName().lastName().company().phone().city().address1().address2().country().province().phone() } }}}
                    it.lines({ it.first(10) }) {
                        it.edges {
                            it.node {
                                it.cost{
                                    it.subtotalAmount {
                                        it.amount().currencyCode()
                                    }.totalAmount {
                                        it.currencyCode().amount()
                                    }
                                }
                                    .quantity()
                                    .merchandise {

                                    it.onProductVariant {
                                       it.sellingPlanAllocations({it.first(10)}) {
                                            it.edges{
                                               it.node {
                                                  it.sellingPlan() {
                                                       it.id()
                                                  }
                                              }
                                           }
                                      }
                                        it.title()

                                            .quantityAvailable()
                                            .availableForSale()
                                            .currentlyNotInStock()
                                            .price{
                                            p->p.amount().currencyCode()
                                        }
                                            .selectedOptions {
                                                it.name().value()
                                            }
                                            .image({ img ->
                                                img
                                                    .url().width().height()
                                            }
                                            )
                                            .product {
                                                p->p.title()
                                            }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
