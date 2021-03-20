package com.htetznaing.boycottchina
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.features.registerImagePicker
import com.esafirm.imagepicker.model.Image

class ImagePickerProxy: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cur = MyApplication.sharedPreferences.getInt(Constants.APP_THEME_KEY, 0)
        val config = ImagePickerConfig {
            mode = ImagePickerMode.SINGLE // default is multi image mode
            isFolderMode = true // set folder mode (false by default)
            if (cur != 0)
                theme = cur
            returnMode = ReturnMode.ALL
            isShowCamera = false // show camera or not (true by default)
        }

        val launcher = registerImagePicker { result: List<Image> ->
            Constants.selectedImage = result.firstOrNull()
            finish()
        }

        launcher.launch(config)
    }
}