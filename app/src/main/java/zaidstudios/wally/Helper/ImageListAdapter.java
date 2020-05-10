package zaidstudios.wally.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import zaidstudios.wally.R;

/**
 * Created by zaid on 23/4/18.
 */

public class ImageListAdapter extends ArrayAdapter {
    private Context context;
    private LayoutInflater inflater;
    ImageView imageView;

    private List<String> imageUrls;

    public ImageListAdapter(Context context, List<String> imageUrls, ImageView imageView) {
        super(context, R.layout.grid_item, imageUrls);

        this.context = context;
        this.imageUrls = imageUrls;
        this.imageView = imageView;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
        }
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        convertView.startAnimation(anim);

        Glide.with(context).load(imageUrls.get(position))
                .apply(new RequestOptions().placeholder(R.drawable.placeholder1).error(R.drawable.error)/*.override(180,180)*/.centerCrop()).into((ImageView) convertView);
        return convertView;
    }
}