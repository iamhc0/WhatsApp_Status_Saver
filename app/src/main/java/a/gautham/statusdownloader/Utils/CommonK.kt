package a.gautham.statusdownloader.Utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

/**
 * Created by Hamza Chaudhary
 * Sr. Software Engineer Android
 * Created on 30 Aug,2022 11:52
 * Copyright (c) All rights reserved.
 */


fun ContentResolver.getRealPathFromMediaData(data: Uri?): String? {
    data ?: return ""

    var cursor: Cursor? = null
    try {
        cursor = query(
            data,
            arrayOf(MediaStore.Images.Media.DATA),
            null, null, null
        )

        val col = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()

        return col?.let { cursor?.getString(it) }
    } finally {
        cursor?.close()
    }
}