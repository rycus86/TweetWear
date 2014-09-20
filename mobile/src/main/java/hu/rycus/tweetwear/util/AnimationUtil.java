package hu.rycus.tweetwear.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class AnimationUtil {

    public static Animation.AnimationListener newOnAnimationEndListener(final Runnable task) {
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(final Animation animation) {
                task.run();
            }

            @Override
            public void onAnimationStart(final Animation animation) { }
            @Override
            public void onAnimationRepeat(final Animation animation) { }
        };
    }

    public static void fadeOutListItemRemoval(final View view, final Runnable endTask) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();

        final AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setAnimationListener(
                AnimationUtil.newOnAnimationEndListener(new Runnable() {
                    @Override
                    public void run() {
                        initViewState();

                        final ValueAnimator animator =
                                ValueAnimator.ofInt(view.getHeight(), 1).setDuration(300);

                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(final ValueAnimator animation) {
                                lp.height = (Integer) animation.getAnimatedValue();
                                view.setLayoutParams(lp);
                            }
                        });

                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(final Animator animation) {
                                endTask.run();
                                restoreViewState();
                            }
                        });

                        animator.start();
                    }

                    private void initViewState() {
                        view.setAlpha(0f);
                    }

                    private void restoreViewState() {
                        view.setAlpha(1f);

                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        view.setLayoutParams(lp);
                    }
                }));

        view.startAnimation(alphaAnimation);
    }

}
