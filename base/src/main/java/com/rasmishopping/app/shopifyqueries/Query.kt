package com.rasmishopping.app.shopifyqueries
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.*
import com.shopify.buy3.Storefront.ProductQuery.*
import com.shopify.graphql.support.ID
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
object Query {
    private val TAG = "Query"
    val shopDetails: QueryRootQuery
        get() = query { q -> q.shop { shop -> shop.name().paymentSettings { pay -> pay.currencyCode().countryCode().cardVaultUrl() }.shipsToCountries()} }
    val CountryCodeDetails: QueryRootQuery
        get() = query { countrycode ->
            countrycode
                .localization { localisation ->
                    localisation.availableCountries { availablecountries -> availablecountries.isoCode().name() }
                    localisation.availableLanguages { it.isoCode().name().endonymName()}
                    localisation.language { it.isoCode().name().endonymName()}
                }
        }
    fun filterdetails(directive: List<InContextDirective>? = null): QueryRootQuery {
        return query(directive) { filterdata ->
            filterdata.collection( {h->h.handle(MagePrefs.getHandle())},{p->p.products({ prod -> prod.first(100)},{p->p.filters { f->f.id().label().type().values { v->v.input().count().label().id() } }})})
        }
    }
    fun getMenuByHandle(handle: String): QueryRootQuery {
        val menudetails: QueryRootQuery = query() { q ->
            q.menu(handle
            ) { shop ->
                shop.title().itemsCount().items {
                    it.title().type().url().tags().resourceId()
                        .items {
                            it.title().type().url().tags().resourceId()
                                .items {
                                    it.title().type().url().tags().resourceId()
                                        .items {
                                            it.title().type().url().tags().resourceId()
                                                .items {
                                                    it.title().type().url().tags().resourceId()
                                                        .items {
                                                            it.title().type().url().tags()
                                                                .resourceId()
                                                                .items {
                                                                    it.title().type().url()
                                                                        .tags().resourceId()
                                                                        .items {
                                                                            it.title().type()
                                                                                .url().tags()
                                                                                .resourceId()
                                                                                .items {
                                                                                    it.title()
                                                                                        .type()
                                                                                        .url()
                                                                                        .tags()
                                                                                        .resourceId()
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
        return menudetails
    }
    fun productDefinition(imagetype: String):ProductConnectionQueryDefinition {
        return ProductConnectionQueryDefinition { productdata ->
            productdata
                .edges { edges -> edges
                        .cursor()
                        .node { node -> node
                                .metafield(Constant.namespace, Constant.key) { m ->
                                    m.key()
                                        .namespace()
                                        .reference { r ->
                                            r.onGenericFile { it ->
                                                it.url()
                                            }
                                        }
                                }
                                .handle()
                                .title()
                                .collections({arg->arg.first(100)},{collectionarg->collectionarg.nodes({n->n.handle()})})
                                .requiresSellingPlan()
                                .sellingPlanGroups({ sellingplan -> sellingplan.first(10) }, { s ->
                                    s.edges { m ->
                                        m.node { x ->
                                            x.name()
                                                .options { option -> option.name().values() }
                                                .sellingPlans({ splan -> splan.first(10) },
                                                    { sellingplan ->
                                                        sellingplan.edges { e ->
                                                            e.node { e ->
                                                                e.description()
                                                                    .id()
                                                                    .name()
                                                                    .options { option ->
                                                                        option.name().value()
                                                                    }
                                                                    .priceAdjustments { priceadj ->
                                                                        priceadj.orderCount()
                                                                            .adjustmentValue { adjvalue ->
                                                                                adjvalue.onSellingPlanPercentagePriceAdjustment() { s ->
                                                                                    s.adjustmentPercentage()
                                                                                }
                                                                                    .onSellingPlanFixedAmountPriceAdjustment { f ->
                                                                                        f.adjustmentAmount { p ->
                                                                                            p.amount()
                                                                                                .currencyCode()
                                                                                        }
                                                                                    }
                                                                            }
                                                                    }
                                                            }

                                                        }
                                                    })
                                        }
                                    }

                                })

                                .images({ img -> img.first(10) }, { imag ->
                                    imag.edges { imgedge ->
                                        imgedge
                                            .node { imgnode ->
                                                imgnode
                                                    .url({i->i.transform(Constant.imageConfiguration(imagetype))})
                                                    .height()
                                                    .width()
                                            }
                                    }
                                }
                                )
                                .seo{
                                    it->it.description().title()
                                }
                                .media({ m -> m.first(10) }, { me ->
                                    me.edges { e ->
                                        e.node { n ->
                                            n.onMediaImage { media ->
                                                media.previewImage { p ->
                                                    p.url()
                                                }
                                            }
                                                .onExternalVideo { it ->
                                                    it.embedUrl()
                                                        .originUrl()
                                                        .previewImage {
                                                            it.url()
                                                        }
                                                }
                                                .onVideo(VideoQueryDefinition {
                                                    it.previewImage {
                                                        it.url()
                                                    }.sources { it ->
                                                        it.url()
                                                    }
                                                })
                                                .onModel3d { md ->
                                                    md
                                                        .sources({ s -> s.url() })
                                                        .previewImage({ p -> p.url() })
                                                }
                                        }
                                    }
                                })
                                .availableForSale()
                                .descriptionHtml()
                                .description()
                                .tags()
                                .vendor()
                                .handle()
                                .totalInventory()
                                .variants({ args ->
                                    args
                                        .first(120)
                                }, { variant ->
                                    variant

                                        .edges { variantEdgeQuery ->
                                            variantEdgeQuery
                                                .node { productVariantQuery ->
                                                    productVariantQuery
                                                        .price { price ->
                                                            price.amount().currencyCode()
                                                        }
//                                                        .sellingPlanAllocations({ sellingplan -> sellingplan.first(10) },
//                                                            { s ->
//                                                                s.edges { m ->
//                                                                    m.node { x ->
//                                                                        x.sellingPlan { sellingplan ->
//                                                                            sellingplan
//                                                                                .description()
//                                                                                .id()
//                                                                                .name()
//                                                                                .options { option ->
//                                                                                    option.name().value()
//                                                                                }
//                                                                                .priceAdjustments { priceadj ->
//                                                                                    priceadj.orderCount()
//                                                                                        .adjustmentValue { adjvalue ->
//                                                                                            adjvalue.onSellingPlanPercentagePriceAdjustment() { s ->
//                                                                                                s.adjustmentPercentage()
//                                                                                            }
//                                                                                                .onSellingPlanFixedAmountPriceAdjustment { f ->
//                                                                                                    f.adjustmentAmount { p ->
//                                                                                                        p.amount()
//                                                                                                            .currencyCode()
//                                                                                                    }
//                                                                                                }
//                                                                                        }
//                                                                                }
//                                                                        }
//
//                                                                    }
//                                                                }
//
//
//                                                            })


                                                        .title()
                                                        .product({t->t.title()})
                                                        .quantityAvailable()
                                                        .selectedOptions { select ->
                                                            select.name().value()
                                                        }
                                                        .compareAtPrice { compare ->
                                                            compare.amount().currencyCode()
                                                        }

                                                        .currentlyNotInStock()
                                                        .image { image ->
                                                            image.url()
                                                        }
                                                        .availableForSale()
                                                        .sku()
                                                }
                                        }
                                }
                                )
                                .onlineStoreUrl()
                                .options { op ->
                                    op.name()
                                        .values()
                                }

                        }
                }
                .pageInfo(Storefront.PageInfoQueryDefinition { it.hasNextPage()
                    .startCursor()
                    .endCursor()
                }
                )
        }
    }

    fun recommendedProducts(
        product_id: String,
        directive: List<InContextDirective>? = null
    ): QueryRootQuery {
        return query(directive) { root ->
            root.productRecommendations(ID(product_id), productQuery("recommended"))
        }
    }

    fun productQuery(viewType: String): Storefront.ProductQueryDefinition {
        return Storefront.ProductQueryDefinition { product ->
            product
                .metafield(Constant.namespace, Constant.key) { m ->
                    m.key()
                        .namespace()
                        .reference { r ->
                            r.onGenericFile { it ->
                                it.url()
                            }
                        }
                }
                .collections({arg->arg.first(100)},{collectionarg->collectionarg.nodes({n->n.handle()})})
                .title()
                .images({ img -> img.first(10) }, { imag ->
                    imag.edges { imgedge ->
                        imgedge.node({ imgnode ->
                            imgnode
                                .url({img->img.transform(Constant.imageConfiguration(viewType))})
                                .height()
                                .width()
                        }
                        )
                    }
                }
                )
                .collections({arg->arg.first(100)},{collectionarg->collectionarg.nodes({n->n.handle()})})
                .availableForSale()
                .descriptionHtml()
                .description()
                .totalInventory()
                .tags()
                .requiresSellingPlan()
                .sellingPlanGroups({ sellingplan -> sellingplan.first(10) }, { s ->
                    s.edges { m ->
                        m.node { x ->
                            x.name()
                                .options { option -> option.name().values() }
                                .sellingPlans({ splan -> splan.first(10) }, { sellingplan ->
                                    sellingplan.edges { e ->
                                        e.node { e ->
                                            e.description()
                                                .id()
                                                .name()
                                                .options { option -> option.name().value() }
                                                .priceAdjustments { priceadj ->
                                                    priceadj.orderCount()
                                                        .adjustmentValue { adjvalue ->
                                                            adjvalue.onSellingPlanPercentagePriceAdjustment() { s ->
                                                                s.adjustmentPercentage()
                                                            }
                                                                .onSellingPlanFixedAmountPriceAdjustment { f ->
                                                                    f.adjustmentAmount { p ->
                                                                        p.amount()
                                                                            .currencyCode()
                                                                    }
                                                                }
                                                        }
                                                }
                                        }

                                    }
                                })
                        }
                    }
                })

                .handle()
                .media({ m -> m.first(10) }, { me ->
                    me.edges({ e ->
                        e.node({ n ->
                            n.onMediaImage { media ->
                                media.previewImage { p ->
                                    p.url({img->img.transform(Constant.imageConfiguration(viewType))}).width().height()
                                }
                            }
                                .onExternalVideo { _queryBuilder ->
                                    _queryBuilder
                                        .embedUrl()
                                        .originUrl()
                                        .previewImage {
                                            it.url()
                                        }
                                }
                                .onVideo(VideoQueryDefinition {
                                    it.previewImage {
                                        it.url()
                                    }.sources { it ->
                                        it.url()
                                    }
                                })
                                .onModel3d({ md ->
                                    md
                                        .sources({ s -> s.url() })
                                        .previewImage({ p -> p.url() })
                                })
                        })
                    })
                })
                .vendor()
                .variants({ args ->
                    args
                        .first(120)
                }, { variant ->
                    variant
                        .edges({ variantEdgeQuery ->
                            variantEdgeQuery
                                .node({ productVariantQuery ->
                                    productVariantQuery
                                        .title()
//                                        .sellingPlanAllocations({ sellingplan -> sellingplan.first(10) },
//                                            { s ->
//                                                s.edges { m ->
//                                                    m.node { x ->
//                                                        x.sellingPlan { sellingplan ->
//                                                            sellingplan
//
//                                                                .description()
//                                                                .id()
//                                                                .name()
//                                                                .options { option ->
//                                                                    option.name().value()
//                                                                }
//                                                                .priceAdjustments { priceadj ->
//                                                                    priceadj.orderCount()
//                                                                        .adjustmentValue { adjvalue ->
//                                                                            adjvalue.onSellingPlanPercentagePriceAdjustment() { s ->
//                                                                                s.adjustmentPercentage()
//                                                                            }
//                                                                                .onSellingPlanFixedAmountPriceAdjustment { f ->
//                                                                                    f.adjustmentAmount { p ->
//                                                                                        p.amount()
//                                                                                            .currencyCode()
//                                                                                    }
//                                                                                }
//                                                                        }
//                                                                }
//                                                        }
//
//                                                    }
//                                                }
//
//
//                                        })

                                        .price({ p -> p.amount().currencyCode() })
                                        .quantityAvailable()
                                        .currentlyNotInStock()
                                        .selectedOptions({ select -> select.name().value() })
                                        .compareAtPrice({ c -> c.amount().currencyCode() })
                                        .image(Storefront.ImageQueryDefinition {
                                            it.url()
                                        })
                                        .availableForSale()
                                        .sku()
                                }
                                )
                        }
                        )
                }
                )
                .onlineStoreUrl()
                .options({ op ->
                    op.name()
                        .values()
                }
                )
        }
    }

    fun getProductsById(
        cat_id: String?,
        cursor: String,
        sortby_key: ProductCollectionSortKeys?,
        direction: Boolean,
        number: Int,
        directive: List<InContextDirective>? = null,
        productFiltersarray: ArrayList<ProductFilter>
    ): QueryRootQuery {
        if (productFiltersarray.isEmpty()) {
            val definition: Storefront.CollectionQuery.ProductsArgumentsDefinition
            if (cursor == "nocursor") {
                if (sortby_key != null) {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).sortKey(sortby_key).reverse(direction)
                    }
                } else {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).reverse(direction)
                    }
                }

            } else {
                if (sortby_key != null) {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).after(cursor).sortKey(sortby_key).reverse(direction)
                    }
                } else {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).after(cursor).reverse(direction)
                    }
                }
            }
            return query(directive) { root ->
                root
                    .node(
                        ID(cat_id)
                    ) { rootnode ->
                        rootnode.
                        onCollection { oncollection ->
                            oncollection
                                .handle()
                                .image { image ->
                                    image
                                        .url().width().height()
                                }
                                .title()
                                .products(
                                    definition, productDefinition("productlist")
                                )

                        }
                    }
            }
        } else {
            val definition: Storefront.CollectionQuery.ProductsArgumentsDefinition
            if (cursor == "nocursor") {
                if (sortby_key != null) {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).sortKey(sortby_key).reverse(direction)
                            .filters(productFiltersarray)

                    }

                } else {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).reverse(direction).filters(productFiltersarray)
                    }
                }

            } else {
                if (sortby_key != null) {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).after(cursor).sortKey(sortby_key).reverse(direction)
                            .filters(productFiltersarray)
                    }
                } else {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).after(cursor).reverse(direction)
                            .filters(productFiltersarray)
                    }
                }
            }
            return query(directive) { root ->
                root
                    .node(
                        ID(cat_id)
                    ) { rootnode ->
                        rootnode.onCollection { oncollection ->
                            oncollection
                                .handle()
                                .image { image ->
                                    image
                                        .url()
                                        .width().height()
                                }
                                .title()
                                .products(
                                    definition, productDefinition("productlist")
                                )
                        }
                    }
            }
        }
    }

    fun queryForShopBlog(): QueryRootQuery {
        return query { root ->
            root.blogs({ it.first(250) }) {

                it.edges({ e ->
                    e.node({ n ->
                        n.title().onlineStoreUrl().handle().authors { a -> a.name() }
                    })
                })
            }


        }
    }


    fun getAllProductsByID(
        id: List<ID>,
        directive: List<InContextDirective>? = null
    ,viewType:String): QueryRootQuery {

        return query(directive) { root: QueryRootQuery ->
            root
                .nodes(id) { n: NodeQuery ->
                    n.onProduct { p: ProductQuery ->
                        p.title()
                            .handle()
                            .vendor()
                            .tags()
                            .collections({ getconn: CollectionsArguments -> getconn.first(100) }
                            ) { conn: CollectionConnectionQuery ->
                                conn
                                    .edges { edgeconn: CollectionEdgeQuery ->
                                        edgeconn
                                            .node { nodeconn: CollectionQuery ->
                                                nodeconn
                                                    .title()
                                            }
                                    }
                            }
                            .images({ img: ImagesArguments -> img.first(10) }
                            ) { imag: ImageConnectionQuery ->
                                imag.edges { imgedge: ImageEdgeQuery ->
                                    imgedge
                                        .node { imgnode: ImageQuery ->
                                            imgnode
                                                .url({t->t.transform(Constant.imageConfiguration(viewType))})
                                                .height()
                                                .width()
                                        }
                                }
                            }
                            .totalInventory()
                            .availableForSale()
                            .descriptionHtml()
                            .description()
                            .onlineStoreUrl()
                            .requiresSellingPlan()
                            .sellingPlanGroups({ sellingplan -> sellingplan.first(10) }, { s ->
                                s.edges { m ->
                                    m.node { x ->
                                        x.name()
                                            .options { option -> option.name().values() }
                                            .sellingPlans({ splan -> splan.first(10) },
                                                { sellingplan ->
                                                    sellingplan.edges { e ->
                                                        e.node { e ->
                                                            e.description()
                                                                .id()
                                                                .name()
                                                                .options { option ->
                                                                    option.name().value()
                                                                }
                                                                .priceAdjustments { priceadj ->
                                                                    priceadj.orderCount()
                                                                        .adjustmentValue { adjvalue ->
                                                                            adjvalue.onSellingPlanPercentagePriceAdjustment() { s ->
                                                                                s.adjustmentPercentage()
                                                                            }
                                                                                .onSellingPlanFixedAmountPriceAdjustment { f ->
                                                                                    f.adjustmentAmount { p ->
                                                                                        p.amount()
                                                                                            .currencyCode()
                                                                                    }
                                                                                }
                                                                        }
                                                                }
                                                        }

                                                    }
                                                })
                                    }
                                }
                            })

                            .options { option -> option.name().values() }
                            .handle()
                            .media({ m -> m.first(10) }, { me ->
                                me.edges({ e ->
                                    e.node({ n ->
                                        n.onMediaImage { media ->
                                            media.previewImage { p ->
                                                p.url()
                                            }
                                        }
                                            .onExternalVideo { _queryBuilder ->
                                                _queryBuilder
                                                    .previewImage {
                                                        it.url()
                                                    }
                                            }
                                            .onVideo(VideoQueryDefinition {
                                                it.previewImage {
                                                    it.url()
                                                }.sources { it ->
                                                    it.url()
                                                }
                                            })
                                            .onModel3d({ md ->
                                                md
                                                    .sources({ s -> s.url() })
                                                    .previewImage({ p -> p.url() })
                                            })
                                    })
                                })
                            })
                            .variants({ args: VariantsArguments ->
                                args
                                    .first(120).sortKey(Storefront.ProductVariantSortKeys.TITLE)
                            }
                            ) { variant: ProductVariantConnectionQuery ->
                                variant
                                    .edges { variantEdgeQuery: ProductVariantEdgeQuery ->
                                        variantEdgeQuery
                                            .node { productVariantQuery: ProductVariantQuery ->
                                                productVariantQuery
                                                    .price { price: MoneyV2Query ->
                                                        price.amount().currencyCode()
                                                    }
                                                    .quantityAvailable()
                                                    .currentlyNotInStock()
                                                    .title()
                                                    .selectedOptions { select: SelectedOptionQuery ->
                                                        select.name().value()
                                                    }
                                                    .compareAtPrice { compare: MoneyV2Query ->
                                                        compare.amount().currencyCode()
                                                    }
                                                    .image({ image ->
                                                        image.url().height().width()
                                                    })
                                                    .availableForSale()
                                                    .sku()
                                            }
                                    }
                            }
                    }
                }
        }
    }

    fun getProductsByHandle(
        handle: String,
        cursor: String,
        sortby_key: ProductCollectionSortKeys?,
        direction: Boolean,
        number: Int,
        directive: List<InContextDirective>? = null,
        productFiltersarray: ArrayList<ProductFilter>
    ): QueryRootQuery {
        ProductFilter().availableInput
        if (productFiltersarray.isEmpty()) {
            val definition: Storefront.CollectionQuery.ProductsArgumentsDefinition
            if (cursor == "nocursor") {
                if (sortby_key != null) {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).sortKey(sortby_key).reverse(direction)
                    }
                } else {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).reverse(direction)
                    }
                }

            } else {
                if (sortby_key != null) {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).after(cursor).sortKey(sortby_key).reverse(direction)
                    }
                } else {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).after(cursor).reverse(direction)
                    }
                }
            }
            return query(directive) { root ->
                root.collection ( {c->c.handle(handle)}, { collect ->
                    collect.products(
                        definition,
                        productDefinition("productlist")
                    )
                })
            }
        } else {
            val definition: CollectionQuery.ProductsArgumentsDefinition
            if (cursor == "nocursor") {
                if (sortby_key != null) {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).sortKey(sortby_key).reverse(direction)
                            .filters(productFiltersarray)
                    }
                } else {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).reverse(direction).filters(productFiltersarray)
                    }
                }

            } else {
                if (sortby_key != null) {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).after(cursor).sortKey(sortby_key).reverse(direction)
                            .filters(productFiltersarray)
                    }
                } else {
                    definition = Storefront.CollectionQuery.ProductsArgumentsDefinition { args ->
                        args.first(number).after(cursor).reverse(direction)
                            .filters(productFiltersarray)
                    }
                }
            }
            return query(directive) { root ->
                root.collection ( {c->c.handle(handle)}, { collect ->
                    collect.products(
                        definition,
                        productDefinition("productlist")
                    )
                })
            }
        }
    }

    fun getAllProducts(
        cursor: String,
        sortby_key: ProductSortKeys?,
        direction: Boolean,
        number: Int,
        directive: List<InContextDirective>? = null,
        productFiltersarray: ArrayList<ProductFilter>
    ): QueryRootQuery {

        val shoppro: QueryRootQuery.ProductsArgumentsDefinition
        if (cursor == "nocursor") {
            if (sortby_key != null) {
                shoppro = QueryRootQuery.ProductsArgumentsDefinition { args ->
                    args.first(number).sortKey(sortby_key).reverse(direction)
                }
            } else {
                shoppro = QueryRootQuery.ProductsArgumentsDefinition { args ->
                    args.first(number).reverse(direction)
                }
            }
        } else {
            if (sortby_key != null) {
                shoppro = QueryRootQuery.ProductsArgumentsDefinition { args ->
                    args.first(number).after(cursor).sortKey(sortby_key).reverse(direction)
                }
            } else {
                shoppro = QueryRootQuery.ProductsArgumentsDefinition { args ->
                    args.first(number).after(cursor).reverse(direction)
                }
            }
        }
        return query(directive) { root ->
            root.products(
                shoppro,
                productDefinition("productlist")
            )
        }
    }
    fun getAllProductsForSearch(
        cursor: String,
        number: Int,
        directive: List<InContextDirective>? = null,
        keyword: String,
        sortby_key: ProductSortKeys?,
        direction: Boolean
    ): QueryRootQuery {
        val shoppro: QueryRootQuery.ProductsArgumentsDefinition
        var searchpattern =Constant.getSearchPattern(keyword)
        if (cursor == "nocursor") {
            shoppro = QueryRootQuery.ProductsArgumentsDefinition { args ->
                args.first(number).sortKey(sortby_key).reverse(direction).query(searchpattern)
            }
        } else {
            shoppro = QueryRootQuery.ProductsArgumentsDefinition { args ->
                args.first(number).after(cursor).sortKey(sortby_key).reverse(direction).query(searchpattern)
            }
        }
        return query(directive) { root ->
            root.products(
                shoppro,
                productDefinition("searchlist")
            )
        }
    }
    fun  getCollections(cursor: String,directive: List<InContextDirective>? = null): QueryRootQuery {
        val definition: QueryRootQuery.CollectionsArgumentsDefinition
        if (cursor == "nocursor") {
            definition =
                QueryRootQuery.CollectionsArgumentsDefinition { args -> args.first(250).sortKey(CollectionSortKeys.UPDATED_AT).reverse(true) }
        } else {
            definition = QueryRootQuery.CollectionsArgumentsDefinition { args ->
                args.first(250).after(cursor).sortKey(CollectionSortKeys.UPDATED_AT).reverse(true)
            }
        }
        return query(directive) { root ->
            root.collections(definition, collectiondef)
        }
    }

    private val collectiondef: Storefront.CollectionConnectionQueryDefinition
        get() = Storefront.CollectionConnectionQueryDefinition { collect ->
            collect
                .edges({ edge ->
                    edge
                        .cursor()
                        .node({ node ->
                            node.title()
                                .seo { it->
                                    it.title().description()
                                }.image({ image -> image.url({t->t.transform(Constant.imageConfiguration("colllection"))}).width().height() })
                        })
                })
                .pageInfo(Storefront.PageInfoQueryDefinition { it.hasNextPage()
                    .startCursor()
                    .endCursor()})
        }

    fun getProductById(
        product_id: String,
        directive: List<InContextDirective>? = null
    ): QueryRootQuery {
        return query(directive) { root ->
            root.node(ID(product_id)) { rootnode ->
                rootnode.onProduct(
                    productQuery("productview")
                )
            }
        }
    }
    fun getProductByHandle(
        handle: String,
        directive: List<InContextDirective>? = null
    ): QueryRootQuery {
        return query(directive) { root ->
            root.product({h->h.handle(handle)}, productQuery("productview"))
        }
    }
    fun getCustomerDetails(customeraccestoken: String): QueryRootQuery {
        return query { root ->
            root
                .customer(
                    customeraccestoken
                ) { customerQuery ->
                    customerQuery
                        .firstName()
                        .lastName()
                        .email()
                        .id()
                        .tags()

                }

        }
    }
    fun getOrderList(
        accesstoken: String?,
        cursor: String,
        directive: List<InContextDirective>? = null
    ): QueryRootQuery {

        return query(directive) { root ->
            root
                .customer(
                    accesstoken
                ) { customer ->
                    customer
                        .orders({ args -> order_list(args, cursor) }, { order ->
                            order
                                .nodes({n->n.processedAt()})
                                .edges({ edge ->
                                    edge
                                        .cursor()
                                        .node({ ordernode ->
                                            ordernode
                                                .customerUrl()
                                                .statusUrl()
                                                .name()
                                                .processedAt()
                                                .orderNumber()
                                                .fulfillmentStatus()
                                                .canceledAt()
                                                .cancelReason()
                                                .financialStatus()
                                                .totalRefunded() { _queryBuilder ->
                                                    _queryBuilder.amount().currencyCode()
                                                }.email()
                                                .phone()
                                                .totalPrice { _queryBuilder ->
                                                    _queryBuilder.amount().currencyCode()
                                                }
                                                .shippingAddress { _queryBuilder ->
                                                    _queryBuilder.address1().address2().city()
                                                        .company().country().firstName()
                                                        .lastName()
                                                        .phone().zip().latitude().longitude()
                                                }.totalShippingPrice { _queryBuilder ->
                                                    _queryBuilder.currencyCode().amount()
                                                }
                                                .totalTax { _queryBuilder ->
                                                    _queryBuilder.amount().currencyCode()
                                                }
                                                .subtotalPrice{_queryBuilder ->
                                                    _queryBuilder.amount().currencyCode()}
                                                .lineItems({ arg -> arg.first(150) }, { item ->
                                                    item
                                                        .edges({ itemedge ->
                                                            itemedge
                                                                .node({ n ->
                                                                    n.title().quantity()
                                                                        .variant({ v ->
                                                                            v.product {
                                                                            }
                                                                            v.price({ p ->
                                                                                p.amount()
                                                                                    .currencyCode()
                                                                            })
                                                                                .selectedOptions(
                                                                                    { select ->
                                                                                        select.name()
                                                                                            .value()
                                                                                    })
                                                                                .compareAtPrice(
                                                                                    { c ->
                                                                                        c.amount()
                                                                                            .currencyCode()
                                                                                    })
                                                                                .image(
                                                                                    Storefront.ImageQueryDefinition { it.url() })
                                                                        })
                                                                }
                                                                )
                                                        }
                                                        )
                                                }
                                                )
                                                .shippingAddress({ ship ->
                                                    ship.address1().address2().firstName()
                                                        .lastName().country().city().phone()
                                                        .province().zip()
                                                })

                                        }
                                        )
                                }
                                )
                                .pageInfo(Storefront.PageInfoQueryDefinition { it.hasNextPage()
                                    .startCursor()
                                    .endCursor()
                                })
                        }
                        )
                }
        }
    }

    private fun order_list(
        arg: Storefront.CustomerQuery.OrdersArguments,
        cursor: String
    ): Storefront.CustomerQuery.OrdersArguments {
        val definition: Storefront.CustomerQuery.OrdersArguments
        if (cursor == "nocursor") {
            definition = arg.first(10).reverse(true)
        } else {
            definition = arg.first(10).after(cursor).reverse(true)
        }
        return definition
    }

    fun getAddressList(accesstoken: String?, cursor: String): QueryRootQuery {
        return query { root ->
            root
                .customer(accesstoken) { customer ->
                    customer
                        .addresses({ arg -> address_list(arg, cursor) }, { address ->
                            address
                                .edges({ edge ->
                                    edge
                                        .cursor()
                                        .node({ node ->
                                            node
                                                .firstName().lastName().company().address1()
                                                .address2().city().country().province().phone()
                                                .zip().formattedArea()
                                        }
                                        )
                                }
                                )
                                .pageInfo(Storefront.PageInfoQueryDefinition { it.hasNextPage()
                                    .startCursor()
                                    .endCursor()})
                        })
                }
        }
    }

    private fun address_list(
        arg: Storefront.CustomerQuery.AddressesArguments,
        cursor: String
    ): Storefront.CustomerQuery.AddressesArguments {
        val definitions: Storefront.CustomerQuery.AddressesArguments
        if (cursor == "nocursor")
            definitions = arg.first(10)
        else
            definitions = arg.first(10).after(cursor)

        return definitions
    }
    fun pollCheckoutCompletion(
        paymentId: ID,
        directive: List<InContextDirective>? = null
    ): QueryRootQuery {
        return query(directive) { rootQuery: QueryRootQuery ->
            rootQuery
                .node(
                    paymentId
                ) { nodeQuery: NodeQuery ->
                    nodeQuery
                        .onPayment { paymentQuery: PaymentQuery ->
                            paymentQuery
                                .checkout { checkoutQuery: CheckoutQuery ->
                                    checkoutQuery
                                        .order { orderQuery: OrderQuery ->
                                            orderQuery
                                                .processedAt()
                                                .orderNumber()
                                                .totalPrice({price->price.amount().currencyCode()})
                                        }
                                }
                                .errorMessage()
                                .ready()
                        }
                }
        }
    }
}

