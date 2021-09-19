package com.example.pagerchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pagerchat.Adapters.ChatAdapter
import com.example.pagerchat.models.*
import com.example.pagerchat.utils.isSameDayAs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*


const val NAME="name"
const val UID="uid"
const val IMAGE="photo"
class ChatActivity : AppCompatActivity() {
    private val friendId by lazy {
        intent.getStringExtra(UID)
    }
    private val name by lazy {
        intent.getStringExtra(NAME)
    }
    private val image by lazy {
        intent.getStringExtra(IMAGE)
    }
    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    private val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    lateinit var currentUser: User
    private val messages: MutableList<ChatEvent> = mutableListOf()
    lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)

        nameTv.text=name
        Picasso.get().load(image).into(userImgView)
        listenToMessages()

        val emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(msgEdtv)
        smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }
//        swipeToLoad.setOnRefreshListener {
//            val workerScope = CoroutineScope(Dispatchers.Main)
//            workerScope.launch {
//                delay(2000)
//                swipeToLoad.isRefreshing = false
//            }
//        }

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser=it.toObject(User::class.java)!!
            }

        chatAdapter = ChatAdapter(messages, mCurrentUid)

        msgRv.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        sendBtn.setOnClickListener {
            msgEdtv.text?.let {
                if(it.isNotEmpty()){
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }

    }

    private fun sendMessage(msg: String) {
        val id=getMessages(friendId).push().key
        checkNotNull(id){"Can't be NUll"}
        val msgMap=Message(msg,mCurrentUid,id)
        getMessages(friendId).child(id).setValue(msgMap).addOnSuccessListener {

        }

        updateLastMessage(msgMap, mCurrentUid)
    }

    private fun updateLastMessage(message: Message, mCurrentUid: String) {
        val inboxMap = Inbox(
            message.msg,
            friendId,
            name,
            image,
            message.sentAt,
            0
        )

        getInbox(mCurrentUid, friendId).setValue(inboxMap)

        getInbox(friendId, mCurrentUid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.getValue(Inbox::class.java)
                inboxMap.apply {
                    from = message.senderId
                    name = currentUser.name
                    image = currentUser.thumbImage
                    count = 1
                }
                if (value?.from == message.senderId) {
                    inboxMap.count = value.count + 1
                }
                getInbox(friendId, mCurrentUid).setValue(inboxMap)
            }

        })
    }

    private fun getInbox(toUser: String?, fromUser: String?)= db.reference.child("chats/$toUser/$fromUser")

        private fun getId(friendId: String?):String{
            return if(friendId!! >mCurrentUid){
                mCurrentUid+friendId
            }
            else{
                friendId+mCurrentUid
            }
        }

        private fun getMessages(friendId: String?) = db.reference.child("messages/${getId(friendId)}")

    private fun listenToMessages(){
        getMessages(friendId)
            .orderByKey()
            .addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val msg=snapshot.getValue(Message::class.java)!!
                    addMessage(msg)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun addMessage(msg: Message) {
        val eventBefore=messages.lastOrNull()
        if ((eventBefore != null
                    && !eventBefore.sentAt.isSameDayAs(msg.sentAt))
            || eventBefore == null
        ) {
            messages.add(
                DateHeader(
                    msg.sentAt, this
                )
            )
        }
        messages.add(msg)

        chatAdapter.notifyItemInserted(messages.size)
        msgRv.scrollToPosition(messages.size + 1)

    }


//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

}