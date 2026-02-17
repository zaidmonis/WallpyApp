package zaidstudios.wally.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import zaidstudios.wally.Activities.ImageActivity;
import zaidstudios.wally.R;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {

    private Context context;
    private List<String> wallpaperUrls;
    private static final int IMAGE_ACTIVITY_REQUEST_CODE = 102;

    public WallpaperAdapter(Context context, List<String> wallpaperUrls) {
        this.context = context;
        this.wallpaperUrls = wallpaperUrls;
    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_wallpaper, parent, false);
        return new WallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        String wallpaperUrl = wallpaperUrls.get(position);

        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        holder.itemView.startAnimation(anim);

        Glide.with(context)
                .load(wallpaperUrl)
                .apply(new RequestOptions().placeholder(R.drawable.placeholder1).error(R.drawable.error).centerCrop())
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("url", wallpaperUrl);
            ((Activity) context).startActivityForResult(intent, IMAGE_ACTIVITY_REQUEST_CODE);
        });
    }

    @Override
    public int getItemCount() {
        return wallpaperUrls.size();
    }

    public static class WallpaperViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public WallpaperViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
