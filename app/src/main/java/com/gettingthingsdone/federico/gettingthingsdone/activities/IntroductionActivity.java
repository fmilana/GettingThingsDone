package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

public class IntroductionActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        addSlide(AppIntro2Fragment.newInstance(getResources().getString(R.string.first_intro_fragment_title),
                getResources().getString(R.string.first_intro_fragment_description),
                R.drawable.ic_launcher_foreground_for_intro,
                getResources().getColor(R.color.colorPrimaryDark)));

        addSlide(AppIntro2Fragment.newInstance(getResources().getString(R.string.second_intro_fragment_title),
                getResources().getString(R.string.second_intro_fragment_description),
                R.drawable.ic_second_intro_fragment,
                getResources().getColor(R.color.colorPrimaryDark)));
//
        addSlide(AppIntro2Fragment.newInstance(getResources().getString(R.string.third_intro_fragment_title),
                getResources().getString(R.string.third_intro_fragment_description),
                R.drawable.ic_third_intro_fragment,
                getResources().getColor(R.color.colorPrimaryDark)));

        addSlide(AppIntro2Fragment.newInstance(getResources().getString(R.string.fourth_intro_fragment_title),
                getResources().getString(R.string.fourth_intro_fragment_description),
                R.drawable.ic_fourth_intro_fragment,
                getResources().getColor(R.color.colorPrimaryDark)));

        addSlide(AppIntro2Fragment.newInstance(getResources().getString(R.string.fifth_intro_fragment_title),
                getResources().getString(R.string.fifth_intro_fragment_description),
                R.drawable.ic_fifth_intro_fragment,
                getResources().getColor(R.color.colorPrimaryDark)));

        addSlide(AppIntro2Fragment.newInstance(getResources().getString(R.string.sixth_intro_fragment_title),
                getResources().getString(R.string.sixth_intro_fragment_description),
                R.drawable.ic_done_white_64dp,
                getResources().getColor(R.color.colorPrimaryDark)));

        showSkipButton(false);

//        setFadeAnimation();
//        setZoomAnimation();
//        setFlowAnimation();
//        setSlideOverAnimation();
//        setDepthAnimation();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        Intent intent = new Intent(IntroductionActivity.this, MainFragmentActivity.class);
        IntroductionActivity.this.startActivity(intent);
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}
