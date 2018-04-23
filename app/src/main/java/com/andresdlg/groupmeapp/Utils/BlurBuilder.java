package com.andresdlg.groupmeapp.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * Created by andresdlg on 15/04/18.
 */

public class BlurBuilder {
    private static final float BITMAP_SCALE = 1f;
    private static final float BLUR_RADIUS = 25f;

    public static Bitmap blur(Context context, Bitmap image) {
        int width = Math.round(image.getWidth() *2* BITMAP_SCALE);
        int height = Math.round(image.getHeight() *2* BITMAP_SCALE);

        Bitmap copyBitmap = image.copy(Bitmap.Config.ARGB_8888,true);
        Bitmap inputBitmap = Bitmap.createScaledBitmap(copyBitmap, width, height, false);
        //Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }
}
