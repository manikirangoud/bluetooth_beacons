package com.pam.tools.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

public class UbuntuButton extends AppCompatButton {


    public UbuntuButton(Context context) {
        super(context);
    }

    public UbuntuButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        int textStyle = attrs.getAttributeIntValue(UbuntuTextView.SCHEMA, "textStyle", Typeface.NORMAL);

        if (textStyle == 0) {
            this.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "ubuntu_regular.ttf"));
        } else if (textStyle == 1) {
            this.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "ubuntu_bold.ttf"));
        }
    }

    public UbuntuButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int textStyle = attrs.getAttributeIntValue(UbuntuTextView.SCHEMA, "textStyle", Typeface.NORMAL);

        if (textStyle == 0) {
            this.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "ubuntu_regular.ttf"));
        } else if (textStyle == 1) {
            this.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "ubuntu_bold.ttf"));
        }
    }
}