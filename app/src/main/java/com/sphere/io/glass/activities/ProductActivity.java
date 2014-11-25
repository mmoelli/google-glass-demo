package com.sphere.io.glass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.Slider;
import com.sphere.io.glass.R;
import com.sphere.io.glass.api.SphereApiCaller;
import com.sphere.io.glass.model.Product;
import com.sphere.io.glass.model.ProductResponseWrapper;
import com.sphere.io.glass.utils.Constants;
import com.squareup.picasso.Picasso;

/**
 * Created by Francisco Villalba on 6/11/14.
 */
public class ProductActivity extends BaseActivity {

    private GestureDetector mGestureDetector;
    private final static String TAG = ProductActivity.class.getName();
    private Slider.Indeterminate mSlider;
    private Product mProduct;
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.e(TAG, "CREATED");
        buildView();
        createGestureDetector();
    }
    private void buildView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"RESUMED");
        mSlider = Slider.from(new CardBuilder(this, CardBuilder.Layout.COLUMNS).getView()).startIndeterminate();
        SphereApiCaller.getInstance(this).getProductBySKU(recoverData());
    }

    public void onEvent(ProductResponseWrapper productResponseWrapper){
        if (productResponseWrapper.getProducts().isEmpty()){
            Intent i = new Intent(this, AlertActivity.class);
            i.putExtra(Constants.ERROR_PRODUCT, true);
            startActivity(i);
            finish();
        }else{
            mProduct = productResponseWrapper.getProducts().get(0);
            populateCard();
            Log.e(TAG,mProduct.toString());
            mSlider.hide();
        }
    }

    private void  populateCard() {
        View view = getLayoutInflater().inflate(R.layout.product_layout, null);
        setContentView(view);
        ImageView mImageIv = (ImageView)view.findViewById(R.id.product_layout_iv_item);
        TextView mNameTv = (TextView)view.findViewById(R.id.product_layout_tv_name);
        TextView mdescriptionTv = (TextView)view.findViewById(R.id.product_layout_tv_desc);
        TextView mPriceTv = (TextView)view.findViewById(R.id.product_layout_tv_price);
        mNameTv.setText(mProduct.getName().getName());
        mdescriptionTv.setText(mProduct.getDescription().getText());
        mPriceTv.setText( getString(R.string.product_price,mProduct.getMasterVariant().getPrices().get(0).getValue().getAmount()/100));
        Picasso.with(this).load(mProduct.getMasterVariant().getImages().get(0).getImageURL()).into(mImageIv);
    }

    private String recoverData(){
        Bundle extras = getIntent().getExtras();
        String text ="";
        if (extras != null) {
             text = extras.getString(Constants.KEY_SKU);
        }
        return text;
    }

    private void createGestureDetector() {
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.LONG_PRESS) {
                    displayConfirmationCard();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

    private void displayConfirmationCard(){
        Intent i = new Intent(this, ConfirmationActivity.class);
        i.putExtra(Constants.KEY_PRODUCT, mProduct);
        startActivity(i);
        finish();
    }
}
