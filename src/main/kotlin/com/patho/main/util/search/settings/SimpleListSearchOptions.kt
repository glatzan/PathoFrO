package com.patho.main.util.search.settings

import com.patho.main.common.PredefinedFavouriteList

enum class SimpleListSearchOption private constructor(
        public val disableGroup: Int,
        public val newPatient: Boolean,
        vararg lists: PredefinedFavouriteList) {

    /**
     * Staining list for laboratory
     */
    STAINING_LIST(1, true,
            PredefinedFavouriteList.StainingList,
            PredefinedFavouriteList.StayInStainingList,
            PredefinedFavouriteList.ReStainingList,

            PredefinedFavouriteList.Council,
            PredefinedFavouriteList.CouncilSendRequestMTA,
            PredefinedFavouriteList.CouncilWaitingForReply,
            PredefinedFavouriteList.ScannList),

    /**
     * Diagnosis list for physicians
     */
    DIAGNOSIS_LIST(1, false,
            PredefinedFavouriteList.DiagnosisList,
            PredefinedFavouriteList.ReDiagnosisList,
            PredefinedFavouriteList.StayInDiagnosisList,

            PredefinedFavouriteList.Council,
            PredefinedFavouriteList.CouncilRequest,
            PredefinedFavouriteList.CouncilWaitingForReply,
            PredefinedFavouriteList.CouncilReplyPresent),

    /**
     * Notification list for secretary
     */
    NOTIFICATION_LIST(1, false, PredefinedFavouriteList.NotificationList,
            PredefinedFavouriteList.StayInNotificationList,
            PredefinedFavouriteList.CouncilSendRequestSecretary,
            PredefinedFavouriteList.CouncilWaitingForReply),

    /**
     * Custom list
     */
    CUSTOM_LIST(1, false),

    /**
     * Empty list
     */
    EMPTY_LIST(2),

    /**
     * Search today
     */
    TODAY(2),

    /**
     * Search yesterday
     */
    YESTERDAY(2),

    /**
     * Search current week
     */
    CURRENTWEEK(2),

    /**
     * Search last week
     */
    LASTWEEK(2),

    /**
     * Search current month
     */
    CURRENTMONTH(2),

    /**
     * Search last month
     */
    LASTMONTH(2),

    /**
     * Search specific day
     */
    DAY(2),

    /**
     * Search specific month
     */
    MONTH(2),

    /**
     * Search user defined time
     */
    TIME(2);

    public val lists: Array<out PredefinedFavouriteList> = lists

    private constructor() : this(0, false) {}

    private constructor(disableGroup: Int) : this(disableGroup, false) {}

}
