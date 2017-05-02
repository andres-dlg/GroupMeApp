package com.andresdlg.groupmeapp;

import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.support.annotation.IntegerRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.news_fragment, NewsFragment.class)
                .add(R.string.groups_fragment, GroupsFragment.class)
                .add(R.string.notifications_fragment, NotificationFragment.class)
                .add(R.string.messages_fragment, MessagesFragment.class)
                .create());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        final LayoutInflater inflater = LayoutInflater.from(this);
        final Resources res = getResources();

        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setCustomTabView(new SmartTabLayout.TabProvider(){
            @Override
            public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
                View itemView = inflater.inflate(R.layout.tab_icon, container, false);
                ImageView icon = (ImageView) itemView.findViewById(R.id.custom_tab_icon);

                //Obtengo las metricas de la pantalla
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                //Divido por la cantidad de fragmentos y determino el ancho del imageview que va en
                // cada tab
                icon.getLayoutParams().width = metrics.widthPixels/4;

                switch (position) {
                    case 0:
                        icon.setImageDrawable(res.getDrawable(R.drawable.ic_public_black_24dp));
                        break;
                    case 1:
                        icon.setImageDrawable(res.getDrawable(R.drawable.ic_people_black_24dp));
                        break;
                    case 2:
                        icon.setImageDrawable(res.getDrawable(R.drawable.ic_notifications_active_black_24dp));
                        break;
                    case 3:
                        icon.setImageDrawable(res.getDrawable(R.drawable.ic_message_black_24dp));
                        break;
                    default:
                        throw new IllegalStateException("Invalid position: " + position);
                }
                return itemView;
            }
        });

        viewPagerTab.setViewPager(viewPager);
    }
}
