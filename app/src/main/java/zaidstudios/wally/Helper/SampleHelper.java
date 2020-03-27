package zaidstudios.wally.Helper;

import android.app.Activity;
import android.widget.FrameLayout;

import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;

import zaidstudios.wally.R;

/**
 * Created by zaid on 28/4/18.
 */

public class SampleHelper /*implements View.OnClickListener */{


    private Activity activity;
    private int theme = R.style.AppThemeDark;
    //Intent intentintro;

    private SampleHelper(Activity activity) {
        this.activity = activity;
    }

    public static SampleHelper with(Activity activity) {
        return new SampleHelper(activity);
    }

    public SampleHelper init() {
        activity.setTheme(theme);
        //intentintro = new Intent(activity, MainActivity.class);

        /*activity.findViewById(R.id.dark).setOnClickListener(this);
        activity.findViewById(R.id.light).setOnClickListener(this);
        activity.findViewById(R.id.custom).setOnClickListener(this);*/

        return this;
    }


    public void loadAbout() {
        final FrameLayout flHolder = activity.findViewById(R.id.about);

        AboutBuilder builder = AboutBuilder.with(activity)
                .setAppIcon(R.mipmap.ic_launcher)
                .setAppName(R.string.app_name)
                .setPhoto(R.drawable.users)
                .setCover(R.mipmap.profile_cover)
                .setLinksAnimated(true)
                .setDividerDashGap(13)
                .setName("Mohd Zaid")
                .setSubTitle("Android Developer")
                .setLinksColumnsCount(4)
                .setBrief("I'm warmed of mobile technologies. Ideas maker, curious and nature lover.")
                //.addGooglePlayStoreLink("https://play.google.com/store/apps/details?id=zaidstudios.whatsapptools")
                .addGitHubLink("zaidmonis")
                //.addBitbucketLink("jrvansuita")
                //.addFacebookLink("https://www.facebook.com/mohd.ariz.100")
                .addTwitterLink("heymzaid")
                .addInstagramLink("zaid_monis")
                .addLink(R.mipmap.whatsapp, "WhatsApp", "https://api.whatsapp.com/send?phone=918126648855")
                .addLink(R.mipmap.facebook, "Facebook", "https://www.facebook.com/monis.z.786")
                .addGooglePlusLink("110624103384805717537")
                //.addYoutubeChannelLink("CaseyNeistat")
                //.addDribbbleLink("user")
                .addLink(R.mipmap.linkedin, "LinkedIn", "https://www.linkedin.com/in/moniszaid/")
                .addEmailLink("monis.z.786@gmail.com")
                //.addWhatsappLink("Mohd Zaid", "+918126648855")
                //.addSkypeLink("monis.z.786")
                //.addGoogleLink("Hello")
                //.addAndroidLink("user")
                //.addWebsiteLink("https://dekhomerapackage.com")
                .addFiveStarsAction("zaidstudios.whatsapptools")
                //.addMoreFromMeAction("Zaid Studios")
                .addAction(R.mipmap.google_play_store, "More apps from me", "http://play.google.com/store/apps/dev?id=8144597860499618446")
                .setVersionNameAsAppSubTitle()
                .addShareAction(R.string.app_name)
                .addUpdateAction("zaidstudios.whatsapptools")
                .setActionsColumnsCount(2)
                .addFeedbackAction("monis.z.786@gmail.com")
                //.addPrivacyPolicyAction("http://www.docracy.com/2d0kis6uc2")
                //.addIntroduceAction((Intent) intentintro)
                //.addHelpAction((Intent) null)
                //.addChangeLogAction((Intent) null)
                //.addRemoveAdsAction((Intent) null)
                //.addDonateAction((Intent) null)
                .setWrapScrollView(true)
                .setShowAsCard(true);

        AboutView view = builder.build();

        flHolder.addView(view);
    }




    /*@Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dark:
                if (theme != R.style.AppThemeDark) {
                    theme = R.style.AppThemeDark;
                    activity.recreate();
                }
                break;
            case R.id.light:
                if (theme != R.style.AppThemeLight) {
                    theme = R.style.AppThemeLight;
                    activity.recreate();
                }
                break;

            case R.id.custom:
                if (theme != R.style.AppThemeCustom) {
                    theme = R.style.AppThemeCustom;
                    activity.recreate();
                }
                break;

            default:
                break;
        }

    }*/
}
