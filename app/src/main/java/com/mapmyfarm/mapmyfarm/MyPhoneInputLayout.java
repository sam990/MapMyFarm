package com.mapmyfarm.mapmyfarm;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputLayout;
import com.lamudi.phonefield.PhoneInputLayout;

public class MyPhoneInputLayout extends PhoneInputLayout {

    public interface onNumberChangeListener{
        public void onValid();
        public void onInvalid();
    }

    onNumberChangeListener listener;

    private TextInputLayout mTextInputLayout;

    public MyPhoneInputLayout(Context context) {
        this(context, null);
    }

    public MyPhoneInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyPhoneInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void updateLayoutAttributes() {
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.TOP);
        setOrientation(HORIZONTAL);
    }

    @Override
    protected void prepareView() {
        super.prepareView();
        mTextInputLayout = findViewWithTag(getResources().getString(R.string.com_lamudi_phonefield_til_phone));
        mTextInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isValid()){
                    if(listener != null){
                        listener.onValid();
                    }
                } else {
                    if (listener != null) {
                        listener.onInvalid();
                    }
                }
            }
        });


    }

    @Override
    public int getLayoutResId() {
        return R.layout.phone_input_layout;
    }

    @Override
    public void setHint(int resId) {
        mTextInputLayout.setHint(getContext().getString(resId));
    }

    @Override
    public void setError(String error) {
        if (error == null || error.length() == 0) {
            mTextInputLayout.setErrorEnabled(false);
        } else {
            mTextInputLayout.setErrorEnabled(true);
        }
        mTextInputLayout.setError(error);
    }

    public TextInputLayout getTextInputLayout() {
        return mTextInputLayout;
    }

    public void addNumberChangeListener(onNumberChangeListener l){
        listener = l;
    }

}
