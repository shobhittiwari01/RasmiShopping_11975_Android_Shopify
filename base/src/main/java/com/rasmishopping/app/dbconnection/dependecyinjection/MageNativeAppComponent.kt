package com.rasmishopping.app.dbconnection.dependecyinjection


import com.rasmishopping.app.addresssection.activities.AddressList
import com.rasmishopping.app.basesection.activities.*
import com.rasmishopping.app.basesection.fragments.LeftMenu
import com.rasmishopping.app.basesection.viewmodels.DemoThemeViewModel
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.CouponsListActivity
import com.rasmishopping.app.cartsection.activities.NativeCheckoutAddressPage
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.cartsection.fragment.CardDataActivity
import com.rasmishopping.app.cartsection.fragment.ShippingMethod
import com.rasmishopping.app.checkoutsection.activities.CheckoutWeblink
import com.rasmishopping.app.checkoutsection.activities.OrderSuccessActivity
import com.rasmishopping.app.collectionsection.activities.CollectionList
import com.rasmishopping.app.collectionsection.activities.CollectionListMenu
import com.rasmishopping.app.dashboard.activities.AccountActivity
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.jobservicessection.JobScheduler
import com.rasmishopping.app.jobservicessection.UploadWorker
import com.rasmishopping.app.loginsection.activity.LoginActivity
import com.rasmishopping.app.loginsection.activity.RegistrationActivity
import com.rasmishopping.app.maintenence_section.MaintenenceActivity
import com.rasmishopping.app.ordersection.activities.OrderDetails
import com.rasmishopping.app.ordersection.activities.OrderList
import com.rasmishopping.app.productsection.activities.*
import com.rasmishopping.app.quickadd_section.activities.QuickAddActivity
//import com.rasmishopping.app.searchsection.activities.AlgoliaSearch
import com.rasmishopping.app.searchsection.activities.AutoSearch
import com.rasmishopping.app.userprofilesection.activities.UserProfile
import com.rasmishopping.app.utils.Urls
import com.rasmishopping.app.wishlistsection.activities.WishList
import com.rasmishopping.app.yotporewards.earnrewards.EarnRewardsActivity
import com.rasmishopping.app.yotporewards.earnrewards.FaqsActivity
import com.rasmishopping.app.yotporewards.getrewards.GetRewardsActivity
import com.rasmishopping.app.yotporewards.myrewards.MyRewardsActivity
import com.rasmishopping.app.yotporewards.referfriend.ReferFriendActivity
import com.rasmishopping.app.yotporewards.rewarddashboard.RewardDashboard
import com.rasmishopping.app.yotporewards.withoutlogin.RewardsPointActivity
import dagger.Component
import javax.inject.Singleton

@Component(modules = [UtilsModule::class])
@Singleton
interface MageNativeAppComponent {

    fun doSplashInjection(splash: Splash)
    fun doFilterPageInjection(product: FilterPage)
    fun doProductListInjection(product: ProductList)
    fun doCollectionInjection(collectionList: CollectionList)
    fun doCollectionInjection(collectionList: CollectionListMenu)
    fun doProductViewInjection(product: ProductView)
    fun doThemeselectionInjection(theme: ThemeSelectionActivity)
    fun doJudgeMeReviewInjection(judgeMeCreateReview: JudgeMeCreateReview)
    fun doYotpoReviewInjection(WriteAReview: WriteAReview)
    fun doReviewListInjection(reviewListActivity: AllReviewListActivity)
    fun doAllJudgeMeReviewListInjection(judgeMeReviews: AllJudgeMeReviews)
    fun doAllAliReviewListInjection(aliReviews: AllAliReviewsListActivity)
    fun doZoomActivityInjection(base: ZoomActivity)
    fun doBaseActivityInjection(base: NewBaseActivity)
    fun doMaintenanceActivityInjection(base: MaintenenceActivity)
    fun doNotificationInjection(notification: NotificationActivity)
    fun doWishListActivityInjection(wish: WishList)
    fun doCartListActivityInjection(cart: CartList)
    fun doCouponsListActivityInjection(coupons: CouponsListActivity)
    fun doSubscribeCartListActivityInjection(subscribecart: SubscribeCartList)
    fun doCheckoutWeblinkActivityInjection(cart: CheckoutWeblink)
    fun doAutoSearchActivityInjection(cart: AutoSearch)
 //   fun doAlgoliaSearchActivityInjection(cart: AlgoliaSearch)
    fun doLoginActivtyInjection(loginActivity: LoginActivity)
    fun doRegistrationActivityInjection(registrationActivity: RegistrationActivity)
    fun doLeftMeuInjection(left: LeftMenu)
    fun doUserProfileInjection(profile: UserProfile)
    fun doOrderListInjection(profile: OrderList)
    fun doOrderDetailsInjection(orderDetails: OrderDetails)
    fun doAddressListInjection(addressList: AddressList)
    fun doNewAddressListInjection(addressList: NativeCheckoutAddressPage)
    fun doHomePageInjection(home: HomePage)
    fun doDemoThemeInjection(demothem: DemoActivity)
    fun doHomePageModelInjection(home: HomePageViewModel)
    fun doDemoModelInjection(demothem: DemoThemeViewModel)
    fun orderSuccessInjection(orderSuccessActivity: OrderSuccessActivity)
    fun quickAddInjection(quickAddActivity: QuickAddActivity)
    fun doServiceInjection(job: JobScheduler)
    fun doWorkerInjection(work: UploadWorker)
    fun doURlInjection(urls: Urls)
    fun doRewarsPointsInjection(rewardsPointActivity: RewardsPointActivity)
    fun doRewarsDashbordInjection(rewardsDashboard: RewardDashboard)
    fun doGetRewadsInjection(getRewardsActivity: GetRewardsActivity)
    fun doEarnRewadsInjection(earnRewardsActivity: EarnRewardsActivity)
    fun doReferFriendInjection(referFriendActivity: ReferFriendActivity)
    fun doMyRewardInjection(myRewardsActivity: MyRewardsActivity)
    fun doFaqsInjection(faqsActivity: FaqsActivity)
    fun doAccountPageInjection(accountActivity: AccountActivity)
    fun doCardPageInjection(cardActivity: CardDataActivity)
    fun doShippingPageInjection(shippingActivity: ShippingMethod)

}
