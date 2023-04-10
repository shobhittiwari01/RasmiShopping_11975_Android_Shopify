package com.rasmishopping.app.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.backinstock.app.BackInStockViewModel
import com.cartdiscount.listing.DiscountListingViewModel
import com.google.gson.Gson
import com.shopify.instafeeds.InstafeedViewModel
import com.rasmishopping.app.FlitsDashboard.Profile.profile.CustomerViewModel
import com.rasmishopping.app.FlitsDashboard.StoreCredits.StoreCreditsViewModel
import com.rasmishopping.app.FlitsDashboard.WishlistSection.FlitsWishlistViewModel

import com.rasmishopping.app.addresssection.viewmodels.AddressModel
import com.rasmishopping.app.basesection.viewmodels.DemoThemeViewModel
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.cartsection.viewmodels.CartListViewModel
import com.rasmishopping.app.cartsection.viewmodels.SubscribeCartListModel
import com.rasmishopping.app.checkoutsection.viewmodels.CheckoutWebLinkViewModel
import com.rasmishopping.app.collectionsection.viewmodels.CollectionMenuViewModel
import com.rasmishopping.app.collectionsection.viewmodels.CollectionViewModel
import com.rasmishopping.app.dashboard.viewmodels.DashBoardViewModel
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.loginsection.viewmodels.LoginViewModel
import com.rasmishopping.app.loginsection.viewmodels.RegistrationViewModel
import com.rasmishopping.app.ordersection.viewmodels.OrderDetailsViewModel
import com.rasmishopping.app.ordersection.viewmodels.OrderListViewModel
import com.rasmishopping.app.personalised.viewmodels.PersonalisedViewModel
import com.rasmishopping.app.productsection.viewmodels.FilterModel
import com.rasmishopping.app.productsection.viewmodels.ProductListModel
import com.rasmishopping.app.productsection.viewmodels.ProductViewModel
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.searchsection.viewmodels.SearchListModel
import com.rasmishopping.app.userprofilesection.viewmodels.UserProfileViewModel
import com.rasmishopping.app.wishlistsection.viewmodels.SubscribeWishListVIewModel
import com.rasmishopping.app.wishlistsection.viewmodels.WishListViewModel
import com.rasmishopping.app.yotporewards.earnrewards.EarnRewardsViewModel
import com.rasmishopping.app.yotporewards.getrewards.GetRewardsViewModel
import com.rasmishopping.app.yotporewards.myrewards.MyRewardsViewModel
import com.rasmishopping.app.yotporewards.referfriend.ReferFriendViewModel
import com.rasmishopping.app.yotporewards.rewarddashboard.RewardDashbordViewModel
import com.shopify.zapietapp.ZapietViewModel

import javax.inject.Inject

class ViewModelFactory @Inject
constructor(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            return SplashViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(LeftMenuViewModel::class.java)) {
            return LeftMenuViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(ProductListModel::class.java)) {
            return ProductListModel(repository) as T
        }
        if (modelClass.isAssignableFrom(CollectionViewModel::class.java)) {
            return CollectionViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(CollectionMenuViewModel::class.java)) {
            return CollectionMenuViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            return ProductViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            return RegistrationViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(WishListViewModel::class.java)) {
            return WishListViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(CartListViewModel::class.java)) {
            return CartListViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SubscribeCartListModel::class.java)) {
            return SubscribeCartListModel(repository) as T
        }
        if (modelClass.isAssignableFrom(CheckoutWebLinkViewModel::class.java)) {
            return CheckoutWebLinkViewModel(repository ) as T
        }
        if (modelClass.isAssignableFrom(SearchListModel::class.java)) {
            return SearchListModel(repository) as T
        }
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            return UserProfileViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(OrderListViewModel::class.java)) {
            return OrderListViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(OrderDetailsViewModel::class.java)) {
            return OrderDetailsViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(AddressModel::class.java)) {
            return AddressModel(repository) as T
        }
        if (modelClass.isAssignableFrom(HomePageViewModel::class.java)) {
            return HomePageViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(PersonalisedViewModel::class.java)) {
            return PersonalisedViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(GetRewardsViewModel::class.java)) {
            return GetRewardsViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(EarnRewardsViewModel::class.java)) {
            return EarnRewardsViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(MyRewardsViewModel::class.java)) {
            return MyRewardsViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(RewardDashbordViewModel::class.java)) {
            return RewardDashbordViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(ReferFriendViewModel::class.java)) {
            return ReferFriendViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(FilterModel::class.java)) {
            return FilterModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SubscribeWishListVIewModel::class.java)) {
            return SubscribeWishListVIewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(CustomerViewModel::class.java)) {
            return CustomerViewModel() as T
        }
        if (modelClass.isAssignableFrom(StoreCreditsViewModel::class.java)) {
            return StoreCreditsViewModel() as T
        }
        if (modelClass.isAssignableFrom(InstafeedViewModel::class.java)) {
            return InstafeedViewModel() as T
        }
        if (modelClass.isAssignableFrom(ZapietViewModel::class.java)) {
            return ZapietViewModel() as T
        }
        if (modelClass.isAssignableFrom(BackInStockViewModel::class.java)) {
            return BackInStockViewModel() as T
        }
        if (modelClass.isAssignableFrom(FlitsWishlistViewModel::class.java)) {
            return FlitsWishlistViewModel() as T
        }
        if (modelClass.isAssignableFrom(DashBoardViewModel::class.java)) {
            return DashBoardViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(DemoThemeViewModel::class.java)) {
            return DemoThemeViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(DiscountListingViewModel::class.java)) {
            return DiscountListingViewModel() as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}
