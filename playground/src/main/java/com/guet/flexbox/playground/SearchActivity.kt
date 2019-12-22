package com.guet.flexbox.playground

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.guet.flexbox.compiler.Compiler
import com.guet.flexbox.content.DynamicNode
import com.guet.flexbox.databinding.DataBindingUtils
import com.guet.flexbox.litho.PageHostView
import com.guet.flexbox.widget.EventHandler
import java.util.*
import kotlin.collections.HashSet

class SearchActivity : AppCompatActivity(), EventHandler {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var list: PageHostView
    private lateinit var editText: EditText
    private var popupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        sharedPreferences = getSharedPreferences("history", Context.MODE_PRIVATE)
        list = findViewById(R.id.list)
        list.eventHandler = this
        editText = findViewById(R.id.search)
        editText.apply {
            setOnFocusChangeListener { v, hasFocus ->
                popupWindow?.dismiss()
                popupWindow = null
                if (hasFocus) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE)
                            as ClipboardManager
                    val data = clipboard.primaryClip
                    if (data != null && data.itemCount > 0) {
                        val item = data.getItemAt(0).text
                        val current = (v as TextView).text
                        if (TextUtils.equals(item, current) ||
                                !(item.startsWith("http://") || item.startsWith("https://"))) {
                            return@setOnFocusChangeListener
                        }
                        val window = PopupWindow()
                        val content = layoutInflater.inflate(
                                R.layout.text_popup_window,
                                FrameLayout(this@SearchActivity),
                                false
                        )
                        val text = content.findViewById<TextView>(R.id.text)
                        window.contentView = content
                        window.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        window.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        text.text = item
                        content.setOnClickListener {
                            popupWindow?.dismiss()
                            popupWindow = null
                            handleEvent(item.toString())
                        }
                        v.post {
                            window.showAsDropDown(v,
                                    0,
                                    -v.height / 4,
                                    Gravity.TOP
                            )
                            popupWindow = window
                        }
                    }
                }
            }
            addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable?) {
                    popupWindow?.dismiss()
                    popupWindow = null
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    val text = v as TextView
                    handleEvent(text.text.toString())
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
        }
        val cancel = findViewById<View>(R.id.cancel)
        cancel.setOnClickListener { finishAfterTransition() }
        loadHistory()
    }

    private fun loadHistory() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val gson = Gson()
            val input = resources.assets.open("layout/search/history_list.xml")
            val s = Compiler.compile(input)
            val contentRaw = gson.fromJson(s, DynamicNode::class.java)
            val rawData = sharedPreferences.getStringSet(
                    "history_list",
                    null
            ) ?: emptySet<String>()
            val listData = if (rawData.size > 10) {
                rawData.toList().subList(0, 10)
            } else {
                rawData.toList()
            }
            val data: Map<String, Any> = Collections.singletonMap(
                    "list",
                    listData
            )
            input.close()
            val content = DataBindingUtils.bind(this, contentRaw, data)
            runOnUiThread {
                list.setContentAsync(content)
            }
        }
    }

    private fun handleEvent(key: String) {
        if (key.isEmpty()) {
            return
        }
        val set = HashSet<String>(sharedPreferences.getStringSet(
                "history_list",
                null
        ) ?: emptySet())
        val listData = if (set.size >= 10) {
            set.toMutableList().subList(0, 9)
        } else {
            set.toMutableList()
        }
        listData.add(key)
        sharedPreferences.edit()
                .putStringSet("list", listData.toSortedSet())
                .apply()
        startActivity(Intent(this, OverviewActivity::class.java)
                .apply {
                    putExtra("url", key)
                }
        )
        finish()
    }

    override fun handleEvent(v: View, key: String, value: Any) {
        handleEvent(key)
    }
}
