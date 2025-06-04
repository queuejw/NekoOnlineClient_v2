/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.neko.online.client.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.core.content.ContextCompat
import ru.neko.online.client.R
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.random.Random

/**
 * It's a cat.
 */
class Cat(context: Context, seed: Long, val name: String?, val id: Long?) : Drawable() {

    private var mNotSoRandom: Random = notSoRandom(seed)
    private var mBitmap: Bitmap? = null
    var bodyColor: Int
        private set
    private var mFootType: Int
    private val mBowTie: Boolean
    private var mFirstMessage: String?
    private val catParts: CatParts = CatParts(context)

    init {
        // body color
        this.bodyColor = chooseP(mNotSoRandom, P_BODY_COLORS)
        if (this.bodyColor == 0) this.bodyColor = Color.HSVToColor(
            floatArrayOf(
                mNotSoRandom.nextFloat() * 360f,
                frandrange(mNotSoRandom, 0.5f, 1f),
                frandrange(mNotSoRandom, 0.5f, 1f)
            )
        )

        tint(
            this.bodyColor,
            catParts.body,
            catParts.head,
            catParts.leg1,
            catParts.leg2,
            catParts.leg3,
            catParts.leg4,
            catParts.tail,
            catParts.leftEar,
            catParts.rightEar,
            catParts.foot1,
            catParts.foot2,
            catParts.foot3,
            catParts.foot4,
            catParts.tailCap
        )
        tint(0x20000000, catParts.leg2Shadow, catParts.tailShadow)
        if (isDark(this.bodyColor)) {
            tint(-0x1, catParts.leftEye, catParts.rightEye, catParts.mouth, catParts.nose)
        }
        tint(
            if (isDark(this.bodyColor)) -0x106566 else 0x20D50000,
            catParts.leftEarInside,
            catParts.rightEarInside
        )

        tint(chooseP(mNotSoRandom, P_BELLY_COLORS), catParts.belly)
        tint(chooseP(mNotSoRandom, P_BELLY_COLORS), catParts.back)
        val faceColor = chooseP(mNotSoRandom, P_BELLY_COLORS)
        tint(faceColor, catParts.faceSpot)
        if (!isDark(faceColor)) {
            tint(-0x1000000, catParts.mouth, catParts.nose)
        }

        mFootType = 0
        if (mNotSoRandom.nextFloat() < 0.25f) {
            mFootType = 4
            tint(-0x1, catParts.foot1, catParts.foot2, catParts.foot3, catParts.foot4)
        } else {
            if (mNotSoRandom.nextFloat() < 0.25f) {
                mFootType = 2
                tint(-0x1, catParts.foot1, catParts.foot3)
            } else if (mNotSoRandom.nextFloat() < 0.25f) {
                mFootType = 3 // maybe -2 would be better? meh.
                tint(-0x1, catParts.foot2, catParts.foot4)
            } else if (mNotSoRandom.nextFloat() < 0.1f) {
                mFootType = 1
                tint(
                    -0x1,
                    choose(
                        mNotSoRandom,
                        catParts.foot1,
                        catParts.foot2,
                        catParts.foot3,
                        catParts.foot4
                    ) as Drawable?
                )
            }
        }

        tint(if (mNotSoRandom.nextFloat() < 0.333f) -0x1 else this.bodyColor, catParts.tailCap)

        val capColor =
            chooseP(mNotSoRandom, if (isDark(this.bodyColor)) P_LIGHT_SPOT_COLORS else P_DARK_SPOT_COLORS)
        tint(capColor, catParts.cap)

        //tint(chooseP(nsr, isDark(bodyColor) ? P_LIGHT_SPOT_COLORS : P_DARK_SPOT_COLORS), D.nose);
        val collarColor = chooseP(mNotSoRandom, P_COLLAR_COLORS)
        tint(collarColor, catParts.collar)
        mBowTie = mNotSoRandom.nextFloat() < 0.1f
        tint(if (mBowTie) collarColor else 0, catParts.bowtie)

        val messages = context.resources.getStringArray(
            if (mNotSoRandom.nextFloat() < 0.1f) R.array.rare_cat_messages else R.array.cat_messages
        )
        mFirstMessage = choose(mNotSoRandom, *messages as Array) as String?
        if (mNotSoRandom.nextFloat() < 0.5f) mFirstMessage = mFirstMessage + mFirstMessage + mFirstMessage
    }

    @Synchronized
    private fun notSoRandom(seed: Long): Random {
        mNotSoRandom = Random(seed)
        return mNotSoRandom
    }

    override fun draw(canvas: Canvas) {
        val w = min(bounds.width().toDouble(), bounds.height().toDouble()).toInt()
        val h = w

        if (mBitmap == null || mBitmap!!.width != w || mBitmap!!.height != h) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val bitCanvas = Canvas(mBitmap!!)
            slowDraw(bitCanvas, 0, 0, w, h)
        }
        canvas.drawBitmap(mBitmap!!, 0f, 0f, null)
    }

    private fun slowDraw(canvas: Canvas, x: Int, y: Int, w: Int, h: Int) {
        for (i in catParts.drawingOrder.indices) {
            catParts.drawingOrder[i]?.let {
                it.setBounds(x, y, x + w, y + h)
                it.draw(canvas)
            }
        }
    }

    fun createBitmap(w: Int, h: Int): Bitmap {
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val pt = Paint()
        val hsv = FloatArray(3)
        Color.colorToHSV(bodyColor, hsv)
        hsv[2] = if (hsv[2] > 0.5f) hsv[2] - 0.25f else hsv[2] + 0.25f
        pt.color = Color.HSVToColor(hsv)
        val r = (w / 2).toFloat()
        canvas.drawCircle(r, r, r, pt)
        val m = w / 10
        slowDraw(canvas, m, m, w - m - m, h - m - m)
        return result
    }

    fun createNotificationLargeIcon(context: Context): Icon? {
        val res = context.resources
        val w = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
        val h = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
        return recompressBitmap(createBitmap(w, h))
    }

    override fun setAlpha(i: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    class CatParts(context: Context) {
        var leftEar: Drawable? = ContextCompat.getDrawable(context, R.drawable.left_ear)
        var rightEar: Drawable? = ContextCompat.getDrawable(context, R.drawable.right_ear)
        var rightEarInside: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.right_ear_inside)
        var leftEarInside: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.left_ear_inside)
        var head: Drawable? = ContextCompat.getDrawable(context, R.drawable.head)
        var faceSpot: Drawable? = ContextCompat.getDrawable(context, R.drawable.face_spot)
        var cap: Drawable? = ContextCompat.getDrawable(context, R.drawable.cap)
        var mouth: Drawable? = ContextCompat.getDrawable(context, R.drawable.mouth)
        var body: Drawable? = ContextCompat.getDrawable(context, R.drawable.body)
        var foot1: Drawable? = ContextCompat.getDrawable(context, R.drawable.foot1)
        var leg1: Drawable? = ContextCompat.getDrawable(context, R.drawable.leg1)
        var foot2: Drawable? = ContextCompat.getDrawable(context, R.drawable.foot2)
        var leg2: Drawable? = ContextCompat.getDrawable(context, R.drawable.leg2)
        var foot3: Drawable? = ContextCompat.getDrawable(context, R.drawable.foot3)
        var leg3: Drawable? = ContextCompat.getDrawable(context, R.drawable.leg3)
        var foot4: Drawable? = ContextCompat.getDrawable(context, R.drawable.foot4)
        var leg4: Drawable? = ContextCompat.getDrawable(context, R.drawable.leg4)
        var tail: Drawable? = ContextCompat.getDrawable(context, R.drawable.tail)
        var leg2Shadow: Drawable? = ContextCompat.getDrawable(context, R.drawable.leg2_shadow)
        var tailShadow: Drawable? = ContextCompat.getDrawable(context, R.drawable.tail_shadow)
        var tailCap: Drawable? = ContextCompat.getDrawable(context, R.drawable.tail_cap)
        var belly: Drawable? = ContextCompat.getDrawable(context, R.drawable.belly)
        var back: Drawable? = ContextCompat.getDrawable(context, R.drawable.back)
        var rightEye: Drawable? = ContextCompat.getDrawable(context, R.drawable.right_eye)
        var leftEye: Drawable? = ContextCompat.getDrawable(context, R.drawable.left_eye)
        var nose: Drawable? = ContextCompat.getDrawable(context, R.drawable.nose)
        var bowtie: Drawable? = ContextCompat.getDrawable(context, R.drawable.bowtie)
        var collar: Drawable? = ContextCompat.getDrawable(context, R.drawable.collar)
        var drawingOrder: Array<Drawable?> = getCatDrawingOrder()

        private fun getCatDrawingOrder(): Array<Drawable?> {
            return arrayOf<Drawable?>(
                collar,
                leftEar, leftEarInside, rightEar, rightEarInside,
                head,
                faceSpot,
                cap,
                leftEye, rightEye,
                nose, mouth,
                tail, tailCap, tailShadow,
                foot1, leg1,
                foot2, leg2,
                foot3, leg3,
                foot4, leg4,
                leg2Shadow,
                body, belly,
                bowtie
            )
        }
    }

    companion object {
        val PURR: LongArray = longArrayOf(0, 40, 20, 40, 20, 40, 20, 40, 20, 40, 20, 40)

        val P_BODY_COLORS: IntArray = intArrayOf(
            180, -0xdededf,  // black
            180, -0x1,  // white
            140, -0x9e9e9f,  // gray
            140, -0x86aab8,  // brown
            100, -0x6f5b52,  // steel
            100, -0x63c,  // buff
            100, -0x7100,  // orange
            5, -0xd6490a,  // blue..?
            5, -0x322e,  // pink!?
            5, -0x316c28,  // purple?!?!?
            4, -0xbc5fb9,  // yeah, why not green
            1, 0,  // ?!?!?!
        )
        val P_COLLAR_COLORS: IntArray = intArrayOf(
            250, -0x1,
            250, -0x1000000,
            250, -0xbbcca,
            50, -0xe6892e,
            50, -0x227cb,
            50, -0x47400,
            50, -0xb704f,
            50, -0xb350b0,
        )
        val P_BELLY_COLORS: IntArray = intArrayOf(
            750, 0,
            250, -0x1,
        )
        val P_DARK_SPOT_COLORS: IntArray = intArrayOf(
            700, 0,
            250, -0xdededf,
            50, -0x92b3bf,
        )
        val P_LIGHT_SPOT_COLORS: IntArray = intArrayOf(
            700, 0,
            300, -0x1,
        )

        fun frandrange(r: Random, a: Float, b: Float): Float {
            return (b - a) * r.nextFloat() + a
        }

        fun choose(r: Random, vararg l: Any?): Any? {
            return l[r.nextInt(l.size)]
        }

        @JvmOverloads
        fun chooseP(r: Random, a: IntArray, sum: Int = 1000): Int {
            var pct = r.nextInt(sum)
            val stop = a.size - 2
            var i = 0
            while (i < stop) {
                pct -= a[i]
                if (pct < 0) break
                i += 2
            }
            return a[i + 1]
        }

        fun getColorIndex(q: Int, a: IntArray): Int {
            var i = 1
            while (i < a.size) {
                if (a[i] == q) {
                    return i / 2
                }
                i += 2
            }
            return -1
        }

        fun tint(color: Int, vararg ds: Drawable?) {
            for (d in ds) {
                d?.mutate()?.setTint(color)
            }
        }

        fun isDark(color: Int): Boolean {
            val r = (color and 0xFF0000) shr 16
            val g = (color and 0x00FF00) shr 8
            val b = color and 0x0000FF
            return (r + g + b) < 0x80
        }

        fun recompressBitmap(bitmap: Bitmap): Icon? {
            val ostream = ByteArrayOutputStream(
                bitmap.width * bitmap.height * 2
            ) // guess 50% compression
            val ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream)
            return if (ok) Icon.createWithData(ostream.toByteArray(), 0, ostream.size()) else null
        }
    }
}
