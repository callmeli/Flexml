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
import com.guet.flexbox.eventsystem.EventHandler
import com.guet.flexbox.eventsystem.event.ClickUrlEvent
import com.guet.flexbox.litho.HostingView
import com.guet.flexbox.litho.TemplatePage
import com.guet.flexbox.playground.model.TemplateCompiler
import es.dmoral.toasty.Toasty
import java.util.*
import kotlin.collections.HashSet

class SearchActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var list: HostingView
    private lateinit var editText: EditText
    private var popupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        sharedPreferences = getSharedPreferences("history", Context.MODE_PRIVATE)
        list = findViewById(R.id.list)
        list.eventBus.subscribe(object : EventHandler<ClickUrlEvent> {
            override fun handleEvent(e: ClickUrlEvent): Boolean {
                handleEvent(e.url)
                return true
            }
        })
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
            val input = resources
                    .assets
                    .open("layout/search/history_list.flexml")
                    .use {
                        it.reader().readText()
                    }
            val template = TemplateCompiler.compile(input)
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
            val content = TemplatePage.create(application)
                    .template(template)
                    .data(data)
                    .build()
            runOnUiThread {
                list.templatePage = content
            }
        }
    }

    private fun handleEvent(key: String) {
        if (key.isEmpty() || key.startsWith("http://")) {
            Toasty.warning(this, "地址格式错误，应该为http://开头").show()
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
}
