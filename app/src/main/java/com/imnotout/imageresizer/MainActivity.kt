package com.imnotout.imageresizer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_crop_image_demo.setOnClickListener{
            startActivity<CropImageDemoActivity>()
        }
        btn_create_gallery_demo.setOnClickListener{
            startActivity<CreateGalleryDemoActivity>()
        }
    }
}
