package edu.vt.cs.cs5254.dreamcatcher.util

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.provider.MediaStore

object CameraUtil {

    private const val TAG = "CameraUtil"

    /**
     * Is the camera (image capture) available to this activity?
     */
    fun isCameraAvailable(activity: Activity): Boolean {
        val packageManager: PackageManager = activity.packageManager
        val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                captureImage,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }

    /**
     * Create an intent to capture an image with permission to store
     * the image at {photoUri}.
     *
     */
    fun createCaptureImageIntent(activity: Activity, photoUri: Uri): Intent {
        val packageManager: PackageManager = activity.packageManager

        val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

        val cameraActivities: List<ResolveInfo> =
            packageManager.queryIntentActivities(
                captureImage,
                PackageManager.MATCH_DEFAULT_ONLY
            )

        for (cameraActivity in cameraActivities) {
            activity.grantUriPermission(
                cameraActivity.activityInfo.packageName,
                photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }

        return captureImage
    }

    /**
     * When we receive an image or shutdown a fragment/activity, let us
     * remove previously granted permissions for capturing images to the {photoUri}.
     */
    fun revokeCaptureImagePermissions(activity: Activity, photoUri: Uri) {
        activity.revokeUriPermission(
            photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    /**
     * Obtain a scaled bitmap scaled to the activity default display size
     * stored in the path requested.
     */
    fun getScaledBitmap(path: String, activity: Activity): Bitmap {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)

        return getScaledBitmap(path, size.x, size.y)
    }

    /**
     * Obtain a scaled bitmap scaled to the provided destWidth and destHeight
     * stored in the path requested.
     */
    fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
        // Read in the dimensions of the image on disk
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)

        val srcWidth = options.outWidth.toFloat()
        val srcHeight = options.outHeight.toFloat()

        // Figure out how much to scale down by
        var inSampleSize = 1
        if (srcHeight > destHeight || srcWidth > destWidth) {
            val heightScale = srcHeight / destHeight
            val widthScale = srcWidth / destWidth

            val sampleScale = if (heightScale > widthScale) {
                heightScale
            } else {
                widthScale
            }
            inSampleSize = Math.round(sampleScale)
        }

        options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize

        // Read in and create final bitmap
        return BitmapFactory.decodeFile(path, options)
    }
}