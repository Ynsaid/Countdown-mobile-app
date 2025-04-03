package com.example.countdownapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AddEventActivity : AppCompatActivity() {
    private lateinit var selectDateBtn: EditText
    private lateinit var closeBtn: ImageView
    private lateinit var selectColorBtn: ImageView
    private lateinit var setImageBtn: ImageView
    private lateinit var main: ConstraintLayout
    private lateinit var sendDataBtn: AppCompatButton
    private lateinit var chooseEmojiBtn: ImageView
    private var selectedEmoji: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_event)

        findViewById<View>(android.R.id.content).setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
        selectDateBtn = findViewById(R.id.date_event)
        selectColorBtn = findViewById(R.id.setColor_btn)
        setImageBtn = findViewById(R.id.setImg_btn)
        main = findViewById(R.id.main)
        closeBtn = findViewById(R.id.close_btn)
        chooseEmojiBtn = findViewById(R.id.emoji)
        sendDataBtn = findViewById(R.id.setData_btn)



        selectDateBtn.setOnClickListener {
            setDate(selectDateBtn)
        }

        selectColorBtn.setOnClickListener {
            showDialog("Select a color")
        }

        closeBtn.setOnClickListener {
            finish()
        }

        setImageBtn.setOnClickListener {
            selectImageInAlbum()
        }

        chooseEmojiBtn.setOnClickListener {
            showEmojiPicker()
        }

        sendDataBtn.setOnClickListener {
            sendDataToMainActivity()
        }
    }
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { focus ->
            imm.hideSoftInputFromWindow(focus.windowToken, 0)
        }
    }
    private fun sendDataToMainActivity() {
        val name = findViewById<EditText>(R.id.name_event).text.toString()
        val date = selectDateBtn.text.toString()

        val colorBackground = when (val bg = main.background) {
            is ColorDrawable -> bg.color
            else -> Color.TRANSPARENT
        }

        val imageBackgroundUri: Uri? = (main.background as? BitmapDrawable)?.bitmap?.let {
            saveBitmapToUri(it)
        }

        // Create intent with all data
        Intent().apply {
            putExtra("name", name)
            putExtra("date", date)
            putExtra("emoji", selectedEmoji) // This will now contain the selected emoji
            putExtra("colorBackground", colorBackground)
            imageBackgroundUri?.let { putExtra("imageBackgroundUri", it.toString()) }
            setResult(RESULT_OK, this)
        }
        finish()
    }
    private fun saveBitmapToUri(bitmap: Bitmap): Uri {
        val fileName = "background_image_${System.currentTimeMillis()}.jpg"
        val file = File(cacheDir, fileName)
        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
    }
    private fun showEmojiPicker() {
        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.dialog_emoji_picker)

            val gridView = findViewById<GridView>(R.id.emoji_grid)


            val emojis = listOf(
                "😊", "😄", "😁", "😆", "😅", "😂", "🤣", "😎",
                "👍", "👎", "👌", "✌️", "👋", "👊", "👏", "🙌",
                "🐶", "🐱", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮"
            )

            gridView.adapter = ArrayAdapter(
                this@AddEventActivity,
                android.R.layout.simple_list_item_1,
                emojis
            )

            gridView.setOnItemClickListener { _, _, position, _ ->
                selectedEmoji = emojis[position]
                setEmojiToButton(selectedEmoji)
                dismiss()
            }

            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                (resources.displayMetrics.heightPixels * 0.5).toInt()
            )
        }
        dialog.show()
    }
    private fun setEmojiToButton(emoji: String) {
        val textView = TextView(this).apply {
            text = emoji
            textSize = 24f
            setTextColor(Color.BLACK)
        }

        textView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        textView.layout(0, 0, textView.measuredWidth, textView.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            textView.measuredWidth,
            textView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        textView.draw(canvas)

        chooseEmojiBtn.setImageBitmap(bitmap)
    }
    private fun selectImageInAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(imageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    main.background = BitmapDrawable(resources, bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun showDialog(title: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.custom_layout)

        val body = dialog.findViewById<TextView>(R.id.body)
        body.text = title

        val colorContainer = dialog.findViewById<GridLayout>(R.id.color_container)

        val colors = listOf(
            "#FF0000", "#00FF00", "#0000FF", "#FFFF00",
            "#FFA500", "#800080", "#00FFFF", "#FFC0CB"
        )

        var selectedColor: Int? = null

        colors.forEach { colorHex ->
            val square = AppCompatImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 100
                    height = 100
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setBackgroundColor(Color.parseColor(colorHex))
                setOnClickListener {
                    selectedColor = Color.parseColor(colorHex)
                }
            }
            colorContainer.addView(square)
        }

        val cancelBtn = dialog.findViewById<Button>(R.id.cancel_btn)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        val setBtn = dialog.findViewById<Button>(R.id.set_btn)
        setBtn.setOnClickListener {
            selectedColor?.let {
                main.setBackgroundColor(it)
                dialog.dismiss()
            } ?: run {
                Toast.makeText(this, "Please select a color", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.3).toInt()
        )

        dialog.show()
    }
    private fun setDate(date: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = "$day/${month + 1}/$year"
            date.setText(selectedDate)
        }, year, month, day)
        datePicker.show()
    }
    companion object {
        private const val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
    }
}