package com.maxgen.societyguru.firebaseAccess

import android.content.Context
import android.util.Log
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.maxgen.societyguru.enums.*
import com.maxgen.societyguru.model.*
import com.maxgen.societyguru.model.member.PaidForEventModel
import com.maxgen.societyguru.utils.SharedPreferenceUser
import org.jetbrains.annotations.NotNull


object FireAccess {

    interface CheckListener {
        fun listen(flag: Boolean, error: String?)
    }

    interface SocietyListListener {
        fun listen(flag: Boolean, societyList: List<SocietyModel>, error: String)
    }

    fun userExists(@NotNull email: String, @NotNull listener: CheckListener) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(email)
            .get()
            .addOnSuccessListener {
                listener.listen(it.exists(), "")
            }.addOnFailureListener {
                listener.listen(false, "${it.message}")
            }
    }

    interface MemberInfoListener {
        fun userReceived(flag: Boolean, error: String? = null, model: UserModel? = null)
    }

    fun getUser(email: String, listener: MemberInfoListener) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(email)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.userReceived(false, error.message)
                    return@addSnapshotListener
                }
                if (value != null && value.exists())
                    listener.userReceived(true, null, value.toObject(UserModel::class.java))
                else listener.userReceived(false, "User not found.")
            }

    fun createUser(
        @NotNull email: String,
        @NotNull password: String,
        @NotNull listener: CheckListener
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                listener.listen(true, "")
            }.addOnFailureListener {
                listener.listen(false, "${it.message}")
            }
    }

    fun storeUser(@NotNull userModel: UserModel, @NotNull listener: CheckListener) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(userModel.email)
            .set(userModel)
            .addOnSuccessListener { listener.listen(true, "") }
            .addOnFailureListener { storeUser(userModel, listener) }
    }

    fun getOpenSocieties(@NotNull listener: SocietyListListener) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .whereEqualTo(SocietyModel.SocietyEnum.status.name, SocietyStatus.ACTIVE.name)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.listen(false, ArrayList(), error.localizedMessage)
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) listener.listen(
                    true,
                    value.toObjects(SocietyModel::class.java),
                    ""
                )
                else listener.listen(false, ArrayList(), "No societies found.")

            }
    }

    fun increaseChairman() {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.TOTAL.name)
            .document(General.chairmen.name)
            .update(General.totalChairmen.name, FieldValue.increment(1))
    }

    fun increaseUser() {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.TOTAL.name)
            .document(General.users.name)
            .update(General.totalUsers.name, FieldValue.increment(1))
    }

    fun increaseSociety() {

        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.TOTAL.name)
            .document(General.societies.name)
            .update(General.totalSocieties.name, FieldValue.increment(1))

    }

    interface OnSocietyEntryStatusChanging {
        fun societyEntryStatusChanged(flag: Boolean, error: String? = null)
    }

    fun changeSocietyRegistrationStatus(
        societyId: String,
        status: String,
        listener: OnSocietyEntryStatusChanging
    ) {
        if (societyId != "") {
            val map = HashMap<String, Any>()
            map[SocietyModel.SocietyEnum.status.name] = status
            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
                .document(societyId)
                .update(map)
                .addOnSuccessListener {
                    listener.societyEntryStatusChanged(true)
                }
                .addOnFailureListener { listener.societyEntryStatusChanged(false, it.message) }
        }
    }

    interface OnDeleteSocietyListener {
        fun societyDeleted(flag: Boolean, error: String? = null)
    }

    fun deleteSociety(
        societyId: String,
        listener: OnDeleteSocietyListener
    ) {
        if (societyId != "") {
            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
                .document(societyId)
                .delete()
                .addOnSuccessListener {
                    getDeletedSocietyUsers(societyId)
                    listener.societyDeleted(true)
                }
                .addOnFailureListener { listener.societyDeleted(false, it.message) }
        }
    }

    private fun getDeletedSocietyUsers(societyId: String) {

        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.USERS.name)
            .whereEqualTo(SocietyModel.SocietyEnum.societyId.name, societyId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    deleteSocietyUsers(value.toObjects(UserModel::class.java))
                }
            }
    }

    private fun deleteSocietyUsers(users: List<UserModel>) {
        val batch = FirebaseFirestore.getInstance().batch()
        for (i in users.indices) {
            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
                .document(users[i].email)
                .delete()
                .addOnSuccessListener {
                    val user = FirebaseAuth.getInstance().currentUser

                    // Get auth credentials from the user for re-authentication. The example below shows
                    // email and password credentials but there are multiple possible providers,
                    // such as GoogleAuthProvider or FacebookAuthProvider.

                    // Get auth credentials from the user for re-authentication. The example below shows
                    // email and password credentials but there are multiple possible providers,
                    // such as GoogleAuthProvider or FacebookAuthProvider.
                    val credential = EmailAuthProvider
                        .getCredential(users[i].email, users[i].password)

                    // Prompt the user to re-provide their sign-in credentials

                    // Prompt the user to re-provide their sign-in credentials
                    user!!.reauthenticate(credential)
                        .addOnCompleteListener {
                            user.delete().addOnCompleteListener {
                                Log.d("DELETED", "DELETED:" + users[i].fName + " " + users[i].lName)
                            }
                        }
                        .addOnFailureListener {
                            Log.d("FAILED", "FAILED:" + it.message)

                        }
                }
        }

    }

    /*
        private fun getDeletedSocietyUsers(societyId: String, listener: OnSocietyUsersDeleted) {

            FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.USERS.name)
                .whereEqualTo(SocietyModel.SocietyEnum.societyId.name, societyId)
                .get().addOnSuccessListener {
                    it.forEach { document ->
                        document.reference.delete()
                            .addOnSuccessListener {
                                listener.societyUsersDeleted(true, null)
                            }.addOnFailureListener { error ->
                                listener.societyUsersDeleted(false, error.message)
                            }
                    }
                }
        }*/
    fun increaseSocietyMembers(societyId: String) {
        val map = HashMap<String, Any>()
        map[SocietyModel.SocietyEnum.members.name] = FieldValue.increment(1)
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .document(societyId)
            .update(map)
    }

    private fun getSocietyUserQuery(societyId: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.societyId.name, societyId)
            .whereEqualTo(UserModel.UserEnum.userType.name, UserType.SOCIETY_MEMBER)

    fun getSocietyUserRvAdapterOptions(societyId: String) =
        FirestoreRecyclerOptions.Builder<UserModel>()
            .setQuery(getSocietyUserQuery(societyId), UserModel::class.java)
            .build()

    private fun getSearchedUserQuery(societyId: String, search: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.societyId.name, societyId)
            .whereEqualTo(UserModel.UserEnum.userType.name, UserType.SOCIETY_MEMBER)
            .orderBy(UserModel.UserEnum.searchName.name).startAt(search).endAt("$search\uf8ff")

/*
    private fun getSearchedUserQuery(societyId: String, search: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.societyId.name, societyId)
            .whereEqualTo(UserModel.UserEnum.userType.name, UserType.SOCIETY_MEMBER)
            .whereEqualTo(UserModel.UserEnum.fName.name, search)
 */

    fun getSearchedUserRvAdapterOptions(societyId: String, search: String) =
        FirestoreRecyclerOptions.Builder<UserModel>()
            .setQuery(getSearchedUserQuery(societyId, search), UserModel::class.java)
            .build()

    fun activateUser(model: UserModel, listener: CheckListener) {
        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.status.name] = UserStatus.ACTIVE.name
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(model.email)
            .update(map)
            .addOnSuccessListener {
                listener.listen(true, null)
            }.addOnFailureListener {
                listener.listen(false, it.message)
            }
    }

    fun blockUser(model: UserModel, @NotNull listener: CheckListener) {
        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.status.name] = UserStatus.BLOCKED.name
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(model.email)
            .update(map)
            .addOnSuccessListener {
                listener.listen(true, null)
            }.addOnFailureListener {
                listener.listen(false, it.message)
            }
    }

    fun updateUserToken(email: String, token: String) {
        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.token.name] = token
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(email)
            .update(map)
            .addOnFailureListener {
                updateUserToken(email, token)
            }
    }

    private fun getNoticeQuery(email: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
            .whereEqualTo(NoticeModel.NoticeEnum.from.name, email)
            .orderBy(NoticeModel.NoticeEnum.createdAt.name, Query.Direction.DESCENDING)

    fun getNoticeRvAdapterOptions(email: String) =
        FirestoreRecyclerOptions.Builder<NoticeModel>()
            .setQuery(getNoticeQuery(email), NoticeModel::class.java)
            .build()

    interface NoticeInfoListener {
        fun listen(flag: Boolean, model: NoticeModel? = null);
    }

    fun getNoticeInfo(noticeId: String, listener: NoticeInfoListener) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
            .document(noticeId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.listen(false)
                    return@addSnapshotListener
                }
                if (value != null && value.exists())
                    listener.listen(true, value.toObject(NoticeModel::class.java))
                else listener.listen(false)
            }
    }

    interface SocietyInfoListener {
        fun listenSocietyInfo(model: SocietyModel?, flag: Boolean, error: String? = null);
    }

    fun getSocietyInfo(societyId: String, listener: SocietyInfoListener) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .document(societyId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.listenSocietyInfo(null, false, error.message)
                    return@addSnapshotListener
                }
                if (value != null && value.exists())
                    listener.listenSocietyInfo(value.toObject(SocietyModel::class.java), true)
                else
                    listener.listenSocietyInfo(null, false, "Data not found.")
            }
    }

    interface SocietyChairmanListener {
        fun listenSocietyChairmanInfo(model: UserModel?, flag: Boolean, error: String? = null);
    }

    fun getSocietyChairman(societyId: String, listener: SocietyChairmanListener) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.userType.name, UserType.CHAIRMAN.name)
            .whereEqualTo(UserModel.UserEnum.societyId.name, societyId)
            .limit(1)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.listenSocietyChairmanInfo(null, false, error.message)
                    return@addSnapshotListener
                }

                if (value != null && !value.isEmpty)
                    listener.listenSocietyChairmanInfo(
                        value.toObjects(UserModel::class.java)[0],
                        true
                    )
                else
                    listener.listenSocietyChairmanInfo(null, false, "Chairman Data not found.")
            }
    }

    private fun getNoticeToQuery(noticeId: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
            .document(noticeId)
            .collection(FirebaseCollectionName.NOTICETO.name)

    fun getNoticeSentToRvOptions(noticeId: String) =
        FirestoreRecyclerOptions.Builder<NoticeModel.NoticeTo>()
            .setQuery(getNoticeToQuery(noticeId), NoticeModel.NoticeTo::class.java)
            .build()

    private fun getChairmanQuery() =
        FirebaseFirestore.getInstance().collection("USERS")
            .whereEqualTo(UserModel.UserEnum.userType.name, UserType.CHAIRMAN.name)
            .whereEqualTo(UserModel.UserEnum.status.name, UserStatus.ACTIVE.name)

    fun getChairmanRvOptions() =
        FirestoreRecyclerOptions.Builder<UserCheckboxModel>()
            .setQuery(getChairmanQuery(), UserCheckboxModel::class.java)
            .build()

    private fun getSocietyActiveUserQuery(societyId: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.societyId.name, societyId)
            .whereEqualTo(UserModel.UserEnum.userType.name, UserType.SOCIETY_MEMBER)
            .whereEqualTo(UserModel.UserEnum.status.name, UserStatus.ACTIVE.name)

    fun getSocietyMemberRvCheckboxOptions(societyId: String) =
        FirestoreRecyclerOptions.Builder<UserCheckboxModel>()
            .setQuery(getSocietyActiveUserQuery(societyId), UserCheckboxModel::class.java)
            .build()

    interface NoticeCreatedListener {
        fun noticeCreated(flag: Boolean, error: String? = null)
    }

    private fun setNoticeId(noticeId: String) {
        val map = HashMap<String, Any>()
        map[NoticeModel.NoticeEnum.noticeId.name] = noticeId
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
            .document(noticeId)
            .update(map)
            .addOnFailureListener { setNoticeId(noticeId) }
    }

    fun createNotice(
        model: NoticeModel,
        noticeTo: ArrayList<NoticeModel.NoticeTo>,
        listener: NoticeCreatedListener

    ) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
            .add(model)
            .addOnSuccessListener {
                setNoticeId(it.id)
                sendNoticeToChairman(it, noticeTo, listener)
            }.addOnFailureListener {
                listener.noticeCreated(false, it.message)
            }
    }

    private fun sendNoticeToChairman(
        notice: DocumentReference,
        models: ArrayList<NoticeModel.NoticeTo>,
        listener: NoticeCreatedListener
    ) {
        val batch = FirebaseFirestore.getInstance().batch()
        val colRef = FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
            .document(notice.id)
            .collection(FirebaseCollectionName.NOTICETO.name)

        for (model in models) {
            model.noticeId = notice.id
            batch.set(colRef.document(), model)
        }
        batch.commit().addOnSuccessListener {
            listener.noticeCreated(true)
        }.addOnFailureListener {
            listener.noticeCreated(false, it.message)
            deleteNotice(notice.id)
        }
    }

    //--------------------  sendNoticeToSingleMember   --------------------//
    fun createSingleNotice(
        model: NoticeModel,
        noticeTo: NoticeModel.NoticeTo,
        listener: NoticeCreatedListener

    ) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
            .add(model)
            .addOnSuccessListener {
                setNoticeId(it.id)
                sendNoticeToSingleMember(it, noticeTo, listener)
            }.addOnFailureListener {
                listener.noticeCreated(false, it.message)
            }
    }

    private fun sendNoticeToSingleMember(
        notice: DocumentReference,
        model: NoticeModel.NoticeTo,
        listener: NoticeCreatedListener
    ) {
        val batch = FirebaseFirestore.getInstance().batch()
        val colRef = FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
            .document(notice.id)
            .collection(FirebaseCollectionName.NOTICETO.name)
        model.noticeId = notice.id
        batch.set(colRef.document(), model)
        batch.commit()
            .addOnSuccessListener {
                listener.noticeCreated(true)
            }.addOnFailureListener {
                listener.noticeCreated(false, it.message)
                deleteNotice(notice.id)
            }
    }

    private fun deleteNotice(noticeId: String) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
            .document(noticeId)
            .delete()
            .addOnFailureListener { deleteNotice(noticeId) }
    }

    fun removeUserToken(email: String) {
        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.token.name] = ""
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(email)
            .update(map)
            .addOnFailureListener { removeUserToken(email) }
    }

    interface PendingNoticeListener {
        fun onReceivePendingNotice(data: ArrayList<NoticeModel.NoticeTo>)
    }

    fun getPendingNotice(context: Context, listener: PendingNoticeListener) {
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.NOTICETO.name)
            .whereEqualTo(
                NoticeModel.NoticeEnum.noticeSent.name,
                "NO"
            )
            .whereEqualTo(
                NoticeModel.NoticeEnum.to.name,
                SharedPreferenceUser.getInstance().getUser(context).email
            )
            .get()
            .addOnSuccessListener { value ->
                if (value != null && !value.isEmpty) {
                    val data: ArrayList<NoticeModel.NoticeTo> = ArrayList()
                    for (doc in value.documents) {
                        val model = doc.toObject(NoticeModel.NoticeTo::class.java)
                        model?.let {
                            data.add(it)
                        }
                    }
                    listener.onReceivePendingNotice(data)
                }
            }

    }

    fun convertPendingNoticeToDone(noticeTo: NoticeModel.NoticeTo) {
        val map = HashMap<String, Any>()
        map[NoticeModel.NoticeEnum.noticeSent.name] = "YES"
        map[NoticeModel.NoticeEnum.to.name] = noticeTo.to
        map[NoticeModel.NoticeEnum.noticeId.name] = noticeTo.noticeId

        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
            .document(noticeTo.noticeId)
            .collection(FirebaseCollectionName.NOTICETO.name)
            .whereEqualTo(NoticeModel.NoticeEnum.to.name, noticeTo.to)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("PENDING_NOTICE", "error" + error.message);
                    return@addSnapshotListener
                }

                if (value != null && !value.isEmpty()) {
                    value.documents[0].reference.update(map)
                        .addOnFailureListener {
                            convertPendingNoticeToDone(noticeTo)
                        }
                }
            }

    }

    //--------------------  MemberMaintenanceReceivedRv   --------------------//

    private fun getNoticeReceivedQuery(email: String) =
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.NOTICETO.name)
            .whereEqualTo(NoticeModel.NoticeEnum.to.name, email)
            .orderBy(
                SocietyMaintenanceModel.MaintenanceEnum.createdAt.name,
                Query.Direction.DESCENDING
            )

    fun getNoticeReceivedRvAdapterOptions(email: String) =
        FirestoreRecyclerOptions.Builder<NoticeModel>()
            .setQuery(getNoticeReceivedQuery(email), NoticeModel::class.java)
            .build()

    //------------------------------------------------------------------------------//

    interface OnNoticeReceivedListListener {
        fun getNoticeList(models: MutableList<NoticeModel.NoticeTo>)
    }

    private fun getMemberNoticeReceivedList(
        context: Context,
        listener: OnNoticeReceivedListListener
    ) {
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.NOTICETO.name)
            .whereIn(NoticeModel.NoticeEnum.noticeSent.name, arrayListOf("YES", "NO"))
            .whereEqualTo(
                NoticeModel.NoticeEnum.to.name,
                SharedPreferenceUser.getInstance().getUser(context).email
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    val models = value.toObjects(NoticeModel.NoticeTo::class.java)
                    listener.getNoticeList(models)
                }
            }
    }

    interface OnNoticeInfoQueryBuild {
        fun getNoticeInfoQuery(query: Query)
    }

    private fun getMemberNoticeReceivedQuery(context: Context, listener: OnNoticeInfoQueryBuild) {
        getMemberNoticeReceivedList(context, object : OnNoticeReceivedListListener {
            override fun getNoticeList(models: MutableList<NoticeModel.NoticeTo>) {
                val idArray: ArrayList<String> = ArrayList()
                models.forEach { model -> idArray.add(model.noticeId) }

                listener.getNoticeInfoQuery(
                    FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
                        .whereIn(FieldPath.documentId(), idArray)
                        .orderBy(NoticeModel.NoticeEnum.createdAt.name, Query.Direction.DESCENDING)
                )

                FirebaseFirestore.getInstance().collection(FirebaseCollectionName.NOTICE.name)
                    .whereIn(FieldPath.documentId(), idArray)
                    // .orderBy(NoticeModel.NoticeEnum.createdAt.name, Query.Direction.DESCENDING)
                    .addSnapshotListener { value, error ->
                        if (error != null) return@addSnapshotListener
                        if (value != null && !value.isEmpty) Log.d("TEST", "TEST")
                    }
            }
        })
    }

    interface OnMemberRvOptionsCreatedListener {
        fun getRvOptions(rvOptions: FirestoreRecyclerOptions<NoticeModel>)
    }

    fun getMemberNoticeReceivedRvOptions(
        context: Context,
        listener: OnMemberRvOptionsCreatedListener
    ) {
        getMemberNoticeReceivedQuery(context, object : OnNoticeInfoQueryBuild {
            override fun getNoticeInfoQuery(query: Query) {
                listener.getRvOptions(
                    FirestoreRecyclerOptions.Builder<NoticeModel>()
                        .setQuery(query, NoticeModel::class.java)
                        .build()
                )
            }
        })
    }

    interface OnEventCreatingListener {
        fun eventCreated(flag: Boolean, error: String? = null)
    }

    fun createSocietyEvent(
        model: SocietyEventModel,
        users: java.util.ArrayList<SocietyEventModel.EventTo>, listener: OnEventCreatingListener
    ) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.EVENT.name)
            .add(model)
            .addOnSuccessListener {
                setEventId(it)
                sendEventToUser(it, users, listener)
                listener.eventCreated(true)
            }.addOnFailureListener {
                listener.eventCreated(false, it.message)
            }
    }

    private fun sendEventToUser(
        event: DocumentReference, eventTo: java.util.ArrayList<SocietyEventModel.EventTo>,
        listner: OnEventCreatingListener
    ) {

        val batch = FirebaseFirestore.getInstance().batch()
        val colRef =
            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.EVENT.name)
                .document(event.id)
                .collection(FirebaseCollectionName.EVENTTO.name)
        for (model in eventTo) {
            model.eventId = event.id
            batch.set(colRef.document(), model)
        }
        batch.commit().addOnSuccessListener {
            listner.eventCreated(true)
        }.addOnFailureListener {
            listner.eventCreated(false, it.message)
        }

    }

    private fun setEventId(docRef: DocumentReference) {
        val map = HashMap<String, Any>()
        map[SocietyEventModel.EventEnum.id.name] = docRef.id
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.EVENT.name)
            .document(docRef.id)
            .update(map)
            .addOnFailureListener { setEventId(docRef) }
    }

    private fun societyEventQuery(societyId: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.EVENT.name)
            .whereEqualTo(SocietyEventModel.EventEnum.societyId.name, societyId)
            .orderBy(SocietyEventModel.EventEnum.createdAt.name, Query.Direction.DESCENDING)

    fun societyEventRvOptions(societyId: String) =
        FirestoreRecyclerOptions.Builder<SocietyEventModel>()
            .setQuery(societyEventQuery(societyId), SocietyEventModel::class.java)
            .build()

    interface OnSocietyEventInfoListener {
        fun eventReceived(flag: Boolean, model: SocietyEventModel? = null, error: String? = null)
    }

    fun getSocietyEventInformation(eventId: String, listener: OnSocietyEventInfoListener) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.EVENT.name)
            .document(eventId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.eventReceived(flag = false, error = error.message)
                    return@addSnapshotListener
                }

                if (value != null && value.exists()) {
                    val model = value.toObject(SocietyEventModel::class.java)
                    listener.eventReceived(flag = true, model = model)
                }
            }
    }

    interface OnEventRegisteringListener {
        fun registeredInEvent(flag: Boolean)
    }

    fun registerUserForSocietyEvent(
        userModel: UserModel,
        eventModel: SocietyEventModel,
        paidForEventModel: PaidForEventModel,
        listener: OnEventRegisteringListener
    ) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.EVENT.name)
            .document(eventModel.id)
            .collection(FirebaseCollectionName.USERPAIDFOREVENT.name)
            .add(paidForEventModel)
            .addOnSuccessListener {
                listener.registeredInEvent(true)
            }.addOnFailureListener {
                registerUserForSocietyEvent(userModel, eventModel, paidForEventModel, listener)
            }

        val map = HashMap<String, Any>()
        map[SocietyEventModel.EventEnum.paidDate.name] = paidForEventModel.paidDate
        map[SocietyEventModel.EventEnum.paidTime.name] = paidForEventModel.paidTime
        map[SocietyEventModel.EventEnum.totalCharge.name] = paidForEventModel.totalCharge
        map[SocietyEventModel.EventEnum.userRegistered.name] = "YES"
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.EVENT.name)
            .document(eventModel.id)
            .collection(FirebaseCollectionName.EVENTTO.name)
            .whereEqualTo(SocietyEventModel.EventEnum.to.name, userModel.email)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("ERRRR", "ERROR : " + error.message)
                }
                if (value != null && !value.isEmpty)
                    value.documents[0].reference.update(map)
                        .addOnSuccessListener {
                            listener.registeredInEvent(true)
                        }.addOnFailureListener {
                            registerUserForSocietyEvent(
                                userModel,
                                eventModel,
                                paidForEventModel,
                                listener
                            )
                        }
            }

    }

    interface OnMaintenancePaidListener {
        fun paidForMaintenance(flag: Boolean)
    }

    fun registerUserPaidForMaintenance(
        userModel: UserModel,
        maintenance: SocietyMaintenanceModel,
        paidMaintenanceModel: SocietyMaintenanceModel.MaintenanceTo,
        listener: OnMaintenancePaidListener
    ) {

        val map = HashMap<String, Any>()
        map[SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.paidDate.name] =
            paidMaintenanceModel.paidDate
        map[SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenancePaid.name] =
            paidMaintenanceModel.maintenancePaid
        map[SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenanceAmount.name] =
            paidMaintenanceModel.maintenanceAmount
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
            .document(maintenance.maintenanceId)
            .collection(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name,
                paidMaintenanceModel.to
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("ERRRR", "ERROR : " + error.message)
                }
                if (value != null && !value.isEmpty)
                    value.documents[0].reference.update(map)
                        .addOnSuccessListener {
                            listener.paidForMaintenance(true)
                        }.addOnFailureListener {
                            registerUserPaidForMaintenance(
                                userModel,
                                maintenance,
                                paidMaintenanceModel,
                                listener
                            )
                        }
            }
    }

    interface OnEventRegistrationStatusListener {
        fun registrationStatus(flag: Boolean)
    }

    fun getEventRegistrationStatus(
        eventId: String,
        userId: String,
        listener: OnEventRegistrationStatusListener
    ) {
        FirebaseFirestore.getInstance()
            .collectionGroup(FirebaseCollectionName.USERPAIDFOREVENT.name)
            .whereEqualTo(PaidForEventModel.PaidEventEnum.eventId.name, eventId)
            .whereEqualTo(PaidForEventModel.PaidEventEnum.userId.name, userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.registrationStatus(false)
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty && value.size() == 1) listener.registrationStatus(
                    true
                )
                else listener.registrationStatus(false)

            }
    }


    interface OnEventRegistrationInfoReceived {
        fun eventRegistrationInfoReceived(model: PaidForEventModel)
    }

    fun getEventRegisteredInfo(
        eventId: String,
        userId: String,
        listener: OnEventRegistrationInfoReceived
    ) {
        FirebaseFirestore.getInstance()
            .collectionGroup(FirebaseCollectionName.USERPAIDFOREVENT.name)
            .whereEqualTo(PaidForEventModel.PaidEventEnum.eventId.name, eventId)
            .whereEqualTo(PaidForEventModel.PaidEventEnum.userId.name, userId)
            .limit(1)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty && value.size() == 1) listener.eventRegistrationInfoReceived(
                    value.toObjects(PaidForEventModel::class.java)[0]
                )
            }

    }

    private fun registeredEventQuery(eventId: String) =
        FirebaseFirestore.getInstance()
            .collection(FirebaseCollectionName.EVENT.name)
            .document(eventId)
            .collection(FirebaseCollectionName.USERPAIDFOREVENT.name)

    fun getRegisteredEventRvOptions(eventId: String) =
        FirestoreRecyclerOptions.Builder<PaidForEventModel>()
            .setQuery(registeredEventQuery(eventId), PaidForEventModel::class.java)
            .build()

    interface OnUserUpdatedListener {
        fun userUpdated(flag: Boolean, model: UserModel? = null, error: String? = null)
    }

    fun updateUserInfo(model: UserModel, listener: OnUserUpdatedListener) {
        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.fName.name] = model.fName
        map[UserModel.UserEnum.lName.name] = model.lName
        map[UserModel.UserEnum.lName.name] = model.lName
        map[UserModel.UserEnum.flatHouseNumber.name] = model.flatHouseNumber
        map[UserModel.UserEnum.mobile.name] = model.mobile
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(model.email)
            .update(map)
            .addOnSuccessListener {
                listener.userUpdated(true, model)
            }.addOnFailureListener {
                listener.userUpdated(false, error = it.message)
            }
    }

    interface OnPaidEventQueryListener {
        fun queryReceived(whereIn: Query)
    }

    private fun getPaidEventsQuery(userId: String, listener: OnPaidEventQueryListener) {
        FirebaseFirestore.getInstance()
            .collectionGroup(FirebaseCollectionName.USERPAIDFOREVENT.name)
            .whereEqualTo(PaidForEventModel.PaidEventEnum.userId.name, userId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                if (value != null && !value.isEmpty) {
                    val models = value.toObjects(PaidForEventModel::class.java)
                    val list = ArrayList<String>()
                    models.forEach { model -> list.add(model.eventId) }
                    listener.queryReceived(
                        FirebaseFirestore.getInstance()
                            .collection(FirebaseCollectionName.EVENT.name)
                            .whereIn(SocietyEventModel.EventEnum.id.name, list)

                    )
                }
            }

    }

    interface OnPaidEventOptionsListener {
        fun optionsReceived(options: FirestoreRecyclerOptions<SocietyEventModel>)
    }

    fun getPaidEventRvOptions(userId: String, listener: OnPaidEventOptionsListener) {
        getPaidEventsQuery(userId, object : OnPaidEventQueryListener {
            override fun queryReceived(whereIn: Query) {
                listener.optionsReceived(
                    FirestoreRecyclerOptions.Builder<SocietyEventModel>()
                        .setQuery(whereIn, SocietyEventModel::class.java)
                        .build()
                )
            }
        })
    }


    //--------------------  CreateMaintenance   --------------------//


    interface OnMaintenanceCreatingListener {
        fun maintenanceCreated(flag: Boolean, error: String? = null)
    }

    fun createSocietyMaintenance(
        model: SocietyMaintenanceModel,
        maintenanceTo: ArrayList<SocietyMaintenanceModel.MaintenanceTo>,
        listener: OnMaintenanceCreatingListener
    ) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
            .add(model)
            .addOnSuccessListener {
                setMaintenanceId(it)
                sendMaintenanceTo(it, maintenanceTo, listener)
            }.addOnFailureListener {
                listener.maintenanceCreated(false, it.message)
            }
    }

    private fun sendMaintenanceTo(
        maintenance: DocumentReference,
        models: ArrayList<SocietyMaintenanceModel.MaintenanceTo>,
        listener: OnMaintenanceCreatingListener
    ) {
        val batch = FirebaseFirestore.getInstance().batch()
        val colRef =
            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
                .document(maintenance.id)
                .collection(FirebaseCollectionName.MAINTENANCETO.name)
        for (model in models) {
            model.maintenanceId = maintenance.id
            batch.set(colRef.document(), model)
        }
        batch.commit().addOnSuccessListener {
            listener.maintenanceCreated(true)
        }.addOnFailureListener {
            listener.maintenanceCreated(false, it.message)
            deleteNotice(maintenance.id)
        }
    }

    private fun setMaintenanceId(docRef: DocumentReference) {
        val map = HashMap<String, Any>()
        map[SocietyMaintenanceModel.MaintenanceEnum.maintenanceId.name] = docRef.id
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
            .document(docRef.id)
            .update(map)
            .addOnFailureListener { setMaintenanceId(docRef) }
    }


    //--------------------  MaintenanceRv   --------------------//


    interface OnMemberMaintenanceRvOptionsCreatedListener {
        fun getRvOptions(rvOptions: FirestoreRecyclerOptions<SocietyMaintenanceModel>)
    }

    fun getMemberMaintenanceReceivedRvOptions(
        context: Context,
        userEmail: String,
        listener: OnMemberMaintenanceRvOptionsCreatedListener
    ) {
        getMemberMaintenanceReceivedQuery(context, userEmail, object : OnMaintenanceInfoQueryBuild {
            override fun getMaintenanceInfoQuery(query: Query) {
                listener.getRvOptions(
                    FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
                        .setQuery(query, SocietyMaintenanceModel::class.java)
                        .build()
                )
            }
        })
    }

    interface OnMaintenanceReceivedListListener {
        fun getMaintenanceList(models: MutableList<SocietyMaintenanceModel.MaintenanceTo>)
    }

    private fun getMemberMaintenanceReceivedList(
        context: Context,
        userEmail: String,
        listener: OnMaintenanceReceivedListListener
    ) {
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereIn(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenancePaid.name,
                arrayListOf("YES", "NO")
            )
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name,
                userEmail
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    val models = value.toObjects(SocietyMaintenanceModel.MaintenanceTo::class.java)
                    listener.getMaintenanceList(models)
                }
            }
    }

    interface OnMaintenanceInfoQueryBuild {
        fun getMaintenanceInfoQuery(query: Query)
    }

    private fun getMemberMaintenanceReceivedQuery(
        context: Context,
        userEmail: String,
        listener: OnMaintenanceInfoQueryBuild
    ) {

        getMemberMaintenanceReceivedList(
            context,
            userEmail,
            object : OnMaintenanceReceivedListListener {
                override fun getMaintenanceList(models: MutableList<SocietyMaintenanceModel.MaintenanceTo>) {
                    val idArray: ArrayList<String> = ArrayList()
                    models.forEach { model -> idArray.add(model.maintenanceId) }
                    listener.getMaintenanceInfoQuery(
                        FirebaseFirestore.getInstance()
                            .collection(FirebaseCollectionName.MAINTENANCE.name)
                            .whereIn(FieldPath.documentId(), idArray)
                            .orderBy(
                                NoticeModel.NoticeEnum.createdAt.name,
                                Query.Direction.DESCENDING
                            )
                    )

                    FirebaseFirestore.getInstance()
                        .collection(FirebaseCollectionName.MAINTENANCE.name)
                        .whereIn(FieldPath.documentId(), idArray)
                        // .orderBy(NoticeModel.NoticeEnum.createdAt.name, Query.Direction.DESCENDING)

                        .addSnapshotListener { value, error ->
                            if (error != null) return@addSnapshotListener
                            if (value != null && !value.isEmpty) Log.d("TEST", "TEST")
                        }

                }
            })
    }

    //--------------------  FilteredMaintenanceRv   --------------------//

    interface OnFilteredMemberMaintenanceRvOptionsCreatedListener {
        fun getRvOptions(rvOptions: FirestoreRecyclerOptions<SocietyMaintenanceModel>)
    }

    fun getFilteredMemberMaintenanceReceivedRvOptions(
        context: Context,
        userEmail: String,
        maintenanceMonth: String,
        listener: OnFilteredMemberMaintenanceRvOptionsCreatedListener
    ) {
        getFilteredMemberMaintenanceReceivedQuery(
            context,
            userEmail,
            maintenanceMonth,
            object : OnFilteredMaintenanceInfoQueryBuild {
                override fun getFilteredMaintenanceInfoQuery(query: Query) {
                    listener.getRvOptions(
                        FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
                            .setQuery(query, SocietyMaintenanceModel::class.java)
                            .build()
                    )

                }
            })
    }

    interface OnFilteredMaintenanceReceivedListListener {
        fun getFilteredMaintenanceList(models: MutableList<SocietyMaintenanceModel.MaintenanceTo>)
    }

    private fun getFilteredMemberMaintenanceReceivedList(
        context: Context,
        userEmail: String,
        maintenanceMonth: String,
        listener: OnFilteredMaintenanceReceivedListListener
    ) {
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereIn(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenancePaid.name,
                arrayListOf("YES", "NO")
            )
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name,
                userEmail
            )
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceEnum.maintenanceMonth.name,
                maintenanceMonth
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    val models = value.toObjects(SocietyMaintenanceModel.MaintenanceTo::class.java)
                    listener.getFilteredMaintenanceList(models)
                }
            }
    }

    interface OnFilteredMaintenanceInfoQueryBuild {
        fun getFilteredMaintenanceInfoQuery(query: Query)
    }

    private fun getFilteredMemberMaintenanceReceivedQuery(
        context: Context,
        userEmail: String,
        maintenanceMonth: String,
        listener: OnFilteredMaintenanceInfoQueryBuild
    ) {

        getFilteredMemberMaintenanceReceivedList(
            context,
            userEmail,
            maintenanceMonth,
            object : OnFilteredMaintenanceReceivedListListener {
                override fun getFilteredMaintenanceList(models: MutableList<SocietyMaintenanceModel.MaintenanceTo>) {
                    val idArray: ArrayList<String> = ArrayList()
                    models.forEach { model -> idArray.add(model.maintenanceId) }
                    listener.getFilteredMaintenanceInfoQuery(
                        FirebaseFirestore.getInstance()
                            .collection(FirebaseCollectionName.MAINTENANCE.name)
                            .whereIn(FieldPath.documentId(), idArray)
                            .orderBy(
                                NoticeModel.NoticeEnum.createdAt.name,
                                Query.Direction.DESCENDING
                            )
                    )

                    FirebaseFirestore.getInstance()
                        .collection(FirebaseCollectionName.MAINTENANCE.name)
                        .whereIn(FieldPath.documentId(), idArray)
                        // .orderBy(NoticeModel.NoticeEnum.createdAt.name, Query.Direction.DESCENDING)
                        .addSnapshotListener { value, error ->
                            if (error != null) return@addSnapshotListener
                            if (value != null && !value.isEmpty) Log.d("TEST", "TEST1")
                        }
                }
            })
    }

    //--------------------  MaintenanceSentRv   --------------------//

    private fun getMaintenanceQuery(email: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
            .whereEqualTo(SocietyMaintenanceModel.MaintenanceEnum.from.name, email)
            .orderBy(
                SocietyMaintenanceModel.MaintenanceEnum.createdAt.name,
                Query.Direction.DESCENDING
            )

    fun getMaintenanceRvAdapterOptions(email: String) =
        FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
            .setQuery(getMaintenanceQuery(email), SocietyMaintenanceModel::class.java)
            .build()

    //--------------------  FilteredMaintenanceSentRv   --------------------//

    private fun getFilteredMaintenanceQuery(email: String, maintenanceMonth: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
            .whereEqualTo(SocietyMaintenanceModel.MaintenanceEnum.from.name, email)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceEnum.maintenanceMonth.name,
                maintenanceMonth
            )
            .orderBy(
                SocietyMaintenanceModel.MaintenanceEnum.createdAt.name,
                Query.Direction.DESCENDING
            )

    fun getFilteredMaintenanceRvAdapterOptions(email: String, maintenanceMonth: String) =
        FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
            .setQuery(
                getFilteredMaintenanceQuery(email, maintenanceMonth),
                SocietyMaintenanceModel::class.java
            )
            .build()

    //--------------------  MemberMaintenanceReceivedRv   --------------------//

    private fun getMaintenanceReceivedQuery(email: String) =
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name, email)
            .orderBy(
                SocietyMaintenanceModel.MaintenanceEnum.createdAt.name,
                Query.Direction.DESCENDING
            )

    fun getMaintenanceReceivedRvAdapterOptions(email: String) =
        FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
            .setQuery(getMaintenanceReceivedQuery(email), SocietyMaintenanceModel::class.java)
            .build()

    //--------------------  FilteredMemberMaintenanceReceivedRv   --------------------//

    private fun getFilteredMaintenanceReceivedQuery(email: String, maintenanceMonth: String) =
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name, email)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceEnum.maintenanceMonth.name,
                maintenanceMonth
            )
            .orderBy(
                SocietyMaintenanceModel.MaintenanceEnum.createdAt.name,
                Query.Direction.DESCENDING
            )

    fun getFilteredMaintenanceReceivedRvAdapterOptions(email: String, maintenanceMonth: String) =
        FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
            .setQuery(
                getFilteredMaintenanceReceivedQuery(email, maintenanceMonth),
                SocietyMaintenanceModel::class.java
            )
            .build()

    //--------------------  MembersPaidMaintenanceRv   --------------------//


    private fun getMembersPaidMaintenanceQuery(maintenanceId: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
            .document(maintenanceId)
            .collection(FirebaseCollectionName.MAINTENANCETO.name)
            .whereIn(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenancePaid.name,
                arrayListOf("YES")
            )
            .whereEqualTo(SocietyMaintenanceModel.MaintenanceEnum.maintenanceId.name, maintenanceId)

    fun getMembersPaidMaintenanceRvAdapterOptions(maintenanceId: String) =
        FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel.MaintenanceTo>()
            .setQuery(
                getMembersPaidMaintenanceQuery(maintenanceId),
                SocietyMaintenanceModel.MaintenanceTo::class.java
            )
            .build()


    //--------------------  MembersDueMaintenanceRv   --------------------//

    private fun getMembersDueMaintenanceQuery(maintenanceId: String) =
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
            .document(maintenanceId)
            .collection(FirebaseCollectionName.MAINTENANCETO.name)
            .whereIn(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenancePaid.name,
                arrayListOf("NO")
            )
            .whereEqualTo(SocietyMaintenanceModel.MaintenanceEnum.maintenanceId.name, maintenanceId)

    fun getMembersDueMaintenanceRvAdapterOptions(maintenanceId: String) =
        FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel.MaintenanceTo>()
            .setQuery(
                getMembersDueMaintenanceQuery(maintenanceId),
                SocietyMaintenanceModel.MaintenanceTo::class.java
            )
            .build()


    //--------------------  PaidMaintenanceRv   --------------------//


    interface OnPaidMaintenanceRvOptionsCreatedListener {
        fun getPaidRvOptions(rvOptions: FirestoreRecyclerOptions<SocietyMaintenanceModel>)
    }

    fun getPaidMaintenanceReceivedRvOptions(
        context: Context,
        listener: OnPaidMaintenanceRvOptionsCreatedListener
    ) {
        getPaidMaintenanceReceivedQuery(context, object : OnPaidMaintenanceInfoQueryBuild {
            override fun getPaidMaintenanceInfoQuery(query: Query) {
                listener.getPaidRvOptions(
                    FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
                        .setQuery(query, SocietyMaintenanceModel::class.java)
                        .build()
                )
            }
        })
    }

    interface OnPaidMaintenanceInfoQueryBuild {
        fun getPaidMaintenanceInfoQuery(query: Query)
    }

    private fun getPaidMaintenanceReceivedQuery(
        context: Context,
        listener: OnPaidMaintenanceInfoQueryBuild
    ) {
        getPaidMaintenanceReceivedList(context, object : OnPaidMaintenanceReceivedListListener {
            override fun getPaidMaintenanceList(models: MutableList<SocietyMaintenanceModel.MaintenanceTo>) {
                val idArray: ArrayList<String> = ArrayList()
                models.forEach { model -> idArray.add(model.maintenanceId) }
                listener.getPaidMaintenanceInfoQuery(
                    FirebaseFirestore.getInstance()
                        .collection(FirebaseCollectionName.MAINTENANCE.name)
                        .whereIn(FieldPath.documentId(), idArray)
                        .orderBy(NoticeModel.NoticeEnum.createdAt.name, Query.Direction.DESCENDING)
                )

                FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
                    .whereIn(FieldPath.documentId(), idArray)
                    // .orderBy(NoticeModel.NoticeEnum.createdAt.name, Query.Direction.DESCENDING)
                    .addSnapshotListener { value, error ->
                        if (error != null) return@addSnapshotListener
                        if (value != null && !value.isEmpty) Log.d("TEST", "TEST")
                    }
            }
        })
    }

    interface OnPaidMaintenanceReceivedListListener {
        fun getPaidMaintenanceList(models: MutableList<SocietyMaintenanceModel.MaintenanceTo>)
    }

    private fun getPaidMaintenanceReceivedList(
        context: Context,
        listener: OnPaidMaintenanceReceivedListListener
    ) {
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereIn(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenancePaid.name,
                arrayListOf("YES")
            )
            .whereEqualTo(
                NoticeModel.NoticeEnum.to.name,
                SharedPreferenceUser.getInstance().getUser(context).email
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    val models = value.toObjects(SocietyMaintenanceModel.MaintenanceTo::class.java)
                    listener.getPaidMaintenanceList(models)
                }
            }
    }


    //--------------------  PaidFilteredMaintenanceRv   --------------------//


    interface OnPaidFilteredMaintenanceRvOptionsCreatedListener {
        fun getPaidRvOptions(rvOptions: FirestoreRecyclerOptions<SocietyMaintenanceModel>)
    }

    fun getPaidFilteredMaintenanceReceivedRvOptions(
        context: Context,
        maintenanceMonth: String,
        listener: OnPaidFilteredMaintenanceRvOptionsCreatedListener
    ) {
        getPaidFilteredMaintenanceReceivedQuery(
            context,
            maintenanceMonth,
            object : OnPaidFilteredMaintenanceInfoQueryBuild {
                override fun getPaidFilteredMaintenanceInfoQuery(query: Query) {
                    listener.getPaidRvOptions(
                        FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
                            .setQuery(query, SocietyMaintenanceModel::class.java)
                            .build()
                    )
                }
            })
    }

    interface OnPaidFilteredMaintenanceInfoQueryBuild {
        fun getPaidFilteredMaintenanceInfoQuery(query: Query)
    }

    private fun getPaidFilteredMaintenanceReceivedQuery(
        context: Context,
        maintenanceMonth: String,
        listener: OnPaidFilteredMaintenanceInfoQueryBuild
    ) {
        getPaidFilteredMaintenanceReceivedList(
            context,
            maintenanceMonth,
            object : OnPaidFilteredMaintenanceReceivedListListener {
                override fun getPaidFilteredMaintenanceList(models: MutableList<SocietyMaintenanceModel.MaintenanceTo>) {
                    val idArray: ArrayList<String> = ArrayList()
                    models.forEach { model -> idArray.add(model.maintenanceId) }
                    listener.getPaidFilteredMaintenanceInfoQuery(
                        FirebaseFirestore.getInstance()
                            .collection(FirebaseCollectionName.MAINTENANCE.name)
                            .whereIn(FieldPath.documentId(), idArray)
                            .orderBy(
                                NoticeModel.NoticeEnum.createdAt.name,
                                Query.Direction.DESCENDING
                            )
                    )

                    FirebaseFirestore.getInstance()
                        .collection(FirebaseCollectionName.MAINTENANCE.name)
                        .whereIn(FieldPath.documentId(), idArray)
                        // .orderBy(NoticeModel.NoticeEnum.createdAt.name, Query.Direction.DESCENDING)
                        .addSnapshotListener { value, error ->
                            if (error != null) return@addSnapshotListener
                            if (value != null && !value.isEmpty) Log.d("TEST", "TEST")
                        }
                }
            })
    }

    interface OnPaidFilteredMaintenanceReceivedListListener {
        fun getPaidFilteredMaintenanceList(models: MutableList<SocietyMaintenanceModel.MaintenanceTo>)
    }

    private fun getPaidFilteredMaintenanceReceivedList(
        context: Context,
        maintenanceMonth: String,
        listener: OnPaidFilteredMaintenanceReceivedListListener
    ) {
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereIn(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenancePaid.name,
                arrayListOf("YES")
            )
            .whereEqualTo(
                NoticeModel.NoticeEnum.to.name,
                SharedPreferenceUser.getInstance().getUser(context).email
            )
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceEnum.maintenanceMonth.name,
                maintenanceMonth
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    val models = value.toObjects(SocietyMaintenanceModel.MaintenanceTo::class.java)
                    listener.getPaidFilteredMaintenanceList(models)
                }
            }

    }


    //--------------------  DueMaintenanceRv   --------------------//


    interface OnDueMaintenanceRvOptionsCreatedListener {
        fun getDueRvOptions(rvOptions: FirestoreRecyclerOptions<SocietyMaintenanceModel>)
    }

    fun getDueMaintenanceReceivedRvOptions(
        context: Context,
        listener: OnDueMaintenanceRvOptionsCreatedListener
    ) {
        getDueMaintenanceReceivedQuery(context, object : OnDueMaintenanceInfoQueryBuild {
            override fun getDueMaintenanceInfoQuery(query: Query) {
                listener.getDueRvOptions(
                    FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
                        .setQuery(query, SocietyMaintenanceModel::class.java)
                        .build()
                )
            }
        })
    }

    interface OnDueMaintenanceInfoQueryBuild {
        fun getDueMaintenanceInfoQuery(query: Query)
    }

    private fun getDueMaintenanceReceivedQuery(
        context: Context,
        listener: OnDueMaintenanceInfoQueryBuild
    ) {
        getDueMaintenanceReceivedList(context, object : OnDueMaintenanceReceivedListListener {
            override fun getDueMaintenanceList(models: MutableList<SocietyMaintenanceModel.MaintenanceTo>) {
                val idArray: ArrayList<String> = ArrayList()
                models.forEach { model -> idArray.add(model.maintenanceId) }
                listener.getDueMaintenanceInfoQuery(
                    FirebaseFirestore.getInstance()
                        .collection(FirebaseCollectionName.MAINTENANCE.name)
                        .whereIn(FieldPath.documentId(), idArray)
                        .orderBy(NoticeModel.NoticeEnum.createdAt.name, Query.Direction.DESCENDING)
                )

                FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
                    .whereIn(FieldPath.documentId(), idArray)
                    // .orderBy(NoticeModel.NoticeEnum.createdAt.name, Query.Direction.DESCENDING)
                    .addSnapshotListener { value, error ->
                        if (error != null) return@addSnapshotListener
                        if (value != null && !value.isEmpty) Log.d("TEST", "TEST")
                    }
            }
        })
    }

    interface OnDueMaintenanceReceivedListListener {
        fun getDueMaintenanceList(models: MutableList<SocietyMaintenanceModel.MaintenanceTo>)
    }

    private fun getDueMaintenanceReceivedList(
        context: Context,
        listener: OnDueMaintenanceReceivedListListener
    ) {
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereIn(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenancePaid.name,
                arrayListOf("NO")
            )
            .whereEqualTo(
                NoticeModel.NoticeEnum.to.name,
                SharedPreferenceUser.getInstance().getUser(context).email
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    val models = value.toObjects(SocietyMaintenanceModel.MaintenanceTo::class.java)
                    listener.getDueMaintenanceList(models)
                }
            }
    }

    interface OnMaintenancePaymentStatusListener {
        fun paymentStatus(flag: Boolean)
    }

    fun getMaintenancePaymentStatus(
        maintenanceId: String,
        userId: String,
        listener: OnMaintenancePaymentStatusListener
    ) {
        FirebaseFirestore.getInstance()
            .collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenanceId.name,
                maintenanceId
            )
            .whereEqualTo(SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name, userId)

            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenancePaid.name,
                "YES"
            )

            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.paymentStatus(false)
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty && value.size() == 1) listener.paymentStatus(
                    true
                )
                else listener.paymentStatus(false)
            }
    }

    interface MaintenanceInfoListener {
        fun listen(
            flag: Boolean,
            model: SocietyMaintenanceModel? = null
        )
    }

    fun getMaintenanceInfo(
        maintenanceId: String,
        userEmail: String,
        listener: MaintenanceInfoListener
    ) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
            .document(maintenanceId)
            .collection(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name,
                userEmail
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.listen(false)
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty)
                    listener.listen(
                        true,
                        value.documents[0].toObject(SocietyMaintenanceModel::class.java)
                    )
                else listener.listen(false)
            }
    }

    interface PaidMaintenanceInfoListener {
        fun listen(
            flag: Boolean,
            paidModel: SocietyMaintenanceModel.MaintenanceTo? = null
        )
    }

    fun getPaidMaintenanceInfo(
        maintenanceId: String,
        userId: String,
        listener: PaidMaintenanceInfoListener
    ) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
            .document(maintenanceId)
            .collection(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name, userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.listen(false)
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    value.documents[0].reference.addSnapshotListener { value, error ->
                        if (error != null) {
                            listener.listen(false)
                            return@addSnapshotListener
                        }
                        if (value != null && value.exists()) {
                            val model =
                                value.toObject(SocietyMaintenanceModel.MaintenanceTo::class.java)

                            listener.listen(
                                true, model
                            )
                        } else listener.listen(false)
                    }
                } else listener.listen(false)
            }
    }

    interface MaintenanceCheckListener {
        fun listen(flag: Boolean, error: String?)
    }

    fun maintenanceExists(Maintenancemonth: String, @NotNull listener: MaintenanceCheckListener) {

        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCE.name)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceEnum.maintenanceMonth.name,
                Maintenancemonth
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    listener.listen(false, error.message)
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty && value.size() == 1) listener.listen(
                    true, "Maintenance Already Created"
                )
                else listener.listen(false, error?.message)
            }

    }

    interface OnNoticeSeenListner {
        fun notSeenNotices(flag: Boolean, error: String? = null)
    }

    fun getNotSeenNotices(noticeId: String, userEmail: String, listner: OnNoticeSeenListner) {
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.NOTICETO.name)
            .whereEqualTo(NoticeModel.NoticeEnum.noticeId.name, noticeId)
            .whereEqualTo(NoticeModel.NoticeEnum.to.name, userEmail)
            .whereEqualTo(NoticeModel.NoticeEnum.seen.name, "NO")
            .addSnapshotListener { value, error ->
                if (error != null) listner.notSeenNotices(false, error.message)
                if (value != null && !value.isEmpty) listner.notSeenNotices(true)
                else listner.notSeenNotices(false)
            }
    }

    interface OnMaintenanceSeenListner {
        fun notSeenMaintenance(flag: Boolean, error: String? = null)
    }

    fun getNotSeenMaintenance(
        maintenanceId: String,
        userEmail: String,
        listner: OnMaintenanceSeenListner
    ) {
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenanceId.name,
                maintenanceId
            )
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name,
                userEmail
            )
            .whereEqualTo(SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.seen.name, "NO")
            .addSnapshotListener { value, error ->
                if (error != null) listner.notSeenMaintenance(false, error.message)
                if (value != null && !value.isEmpty) listner.notSeenMaintenance(true)
                else listner.notSeenMaintenance(false)
            }
    }


    interface OnEventSeenListner {
        fun notSeenEvent(flag: Boolean, error: String? = null)
    }

    fun getNotSeenEvent(
        eventId: String,
        userEmail: String,
        listner: OnEventSeenListner
    ) {
        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.EVENTTO.name)
            .whereEqualTo(
                SocietyEventModel.EventEnum.eventId.name,
                eventId
            )
            .whereEqualTo(
                SocietyEventModel.EventEnum.to.name,
                userEmail
            )
            .whereEqualTo(SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.seen.name, "NO")
            .addSnapshotListener { value, error ->
                if (error != null) listner.notSeenEvent(false, error.message)
                if (value != null && !value.isEmpty) listner.notSeenEvent(true)
                else listner.notSeenEvent(false)
            }
    }

}


