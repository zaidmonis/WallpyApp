package zaidstudios.wally.Helper;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import zaidstudios.wally.Activities.ImageActivity;
import zaidstudios.wally.R;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by zaid on 8/10/18.
 */

public class GlideImageLoader {

    private ImageView mImageView,  mUnloadedImageView;
    private ProgressBar mProgressBar;
    private TextView mtextView;
    private ImageActivity mcontext;

    public GlideImageLoader(ImageView imageView, ProgressBar progressBar, TextView textView, ImageView unLoadedImageView, ImageActivity context) {
        mImageView = imageView;
        mProgressBar = progressBar;
        mtextView = textView;
        mUnloadedImageView = unLoadedImageView;
        mcontext = context;
    }

    public void load(final String url, RequestOptions options) {
        if (url == null || options == null) return;

        onConnecting();

        //set Listener & start
        ProgressAppGlideModule.expect(url, new ProgressAppGlideModule.UIonProgressListener() {
            @Override
            public void onProgress(long bytesRead, long expectedLength) {
                if (mProgressBar != null) {
                    mProgressBar.setProgress((int) (100 * bytesRead / expectedLength));
                    String progress = "Loading HD Image (" +mProgressBar.getProgress() +"%)";
                    mtextView.setText(progress);
                }
            }

            @Override
            public float getGranualityPercentage() {
                return 1.0f;
            }
        });
        //Get Image
        Glide.with(mImageView.getContext())
                .load(url)
                .transition(withCrossFade())
                .apply(options.skipMemoryCache(true))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        ProgressAppGlideModule.forget(url);
                        onFinished();
                        mtextView.setText("Unable to Load!");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ProgressAppGlideModule.forget(url);
                        onFinished();
                        Animation fadeInAnim = AnimationUtils.loadAnimation(mcontext, R.anim.fade_in);
                        Animation fadeOutAnim = AnimationUtils.loadAnimation(mcontext, R.anim.fade_out);
                        mImageView.startAnimation(fadeInAnim);
                        mUnloadedImageView.startAnimation(fadeOutAnim);
                        mtextView.setVisibility(View.GONE);
                        mUnloadedImageView.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mImageView);
    }


    private void onConnecting() {
        if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
    }

    private void onFinished() {
        if (mProgressBar != null && mImageView != null) {
            mProgressBar.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
        }
    }
}