package com.example.el_mozote;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class AppNavigation {
    private AppNavigation() { }

    public static void configure(AppCompatActivity activity, int selectedId) {
        EdgeToEdge.enable(activity);

        View mainView = activity.findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                View toolbar = activity.findViewById(R.id.userToolbar);
                if (toolbar == null) toolbar = activity.findViewById(R.id.screenToolbar);
                if (toolbar != null) {
                    toolbar.setPadding(toolbar.getPaddingLeft(), bars.top, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
                } else {
                    View header = activity.findViewById(R.id.dashboardHeader);
                    if (header != null) {
                        int horizontal = (int) (20 * activity.getResources().getDisplayMetrics().density);
                        int vertical = (int) (18 * activity.getResources().getDisplayMetrics().density);
                        header.setPadding(horizontal, bars.top + vertical, horizontal, vertical);
                    }
                }

                View bottomNav = activity.findViewById(R.id.bottomNavigation);
                if (bottomNav != null) {
                    applyBottomNavigationInsets(bottomNav, bars.bottom);
                }
                return insets;
            });
            ViewCompat.requestApplyInsets(mainView);
        }

        int[] ids = {R.id.nav_home, R.id.nav_vehicles, R.id.nav_reservations, R.id.nav_users, R.id.nav_profile};
        for (int id : ids) {
            View item = activity.findViewById(id);
            if (item == null) continue;
            item.setBackgroundResource(id == selectedId ? R.drawable.nav_item_selected : android.R.color.transparent);
            if (item instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) item;
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    if (child instanceof TextView) {
                        ((TextView) child).setTextColor(activity.getColor(id == selectedId
                                ? R.color.emerald_dark : R.color.slate_muted));
                    } else if (child instanceof ImageView) {
                        ((ImageView) child).setColorFilter(activity.getColor(id == selectedId
                                ? R.color.emerald_primary : R.color.slate_muted));
                    }
                }
            }
            item.setOnClickListener(v -> navigate(activity, id));
        }
    }

    private static void applyBottomNavigationInsets(View bottomNav, int bottomInset) {
        Object saved = bottomNav.getTag();
        BottomNavigationBase base;
        if (saved instanceof BottomNavigationBase) {
            base = (BottomNavigationBase) saved;
        } else {
            ViewGroup.LayoutParams params = bottomNav.getLayoutParams();
            base = new BottomNavigationBase(
                    params == null ? ViewGroup.LayoutParams.WRAP_CONTENT : params.height,
                    bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(),
                    bottomNav.getPaddingRight(), bottomNav.getPaddingBottom());
            bottomNav.setTag(base);
        }

        ViewGroup.LayoutParams params = bottomNav.getLayoutParams();
        if (params != null && base.height >= 0) {
            int adjustedHeight = base.height + bottomInset;
            if (params.height != adjustedHeight) {
                params.height = adjustedHeight;
                bottomNav.setLayoutParams(params);
            }
        }
        bottomNav.setPadding(base.paddingLeft, base.paddingTop, base.paddingRight,
                base.paddingBottom + bottomInset);
    }

    private static final class BottomNavigationBase {
        final int height;
        final int paddingLeft;
        final int paddingTop;
        final int paddingRight;
        final int paddingBottom;

        BottomNavigationBase(int height, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
            this.height = height;
            this.paddingLeft = paddingLeft;
            this.paddingTop = paddingTop;
            this.paddingRight = paddingRight;
            this.paddingBottom = paddingBottom;
        }
    }

    private static void navigate(AppCompatActivity activity, int destinationId) {
        int currentId = activity instanceof MainActivity ? R.id.nav_home
                : activity instanceof VehiclesActivity ? R.id.nav_vehicles
                : activity instanceof ReservationsActivity ? R.id.nav_reservations
                : activity instanceof UserManagementActivity ? R.id.nav_users
                : R.id.nav_profile;
        if (destinationId == currentId) return;

        Class<?> destination = destinationId == R.id.nav_home ? MainActivity.class
                : destinationId == R.id.nav_vehicles ? VehiclesActivity.class
                : destinationId == R.id.nav_reservations ? ReservationsActivity.class
                : destinationId == R.id.nav_users ? UserManagementActivity.class
                : ProfileActivity.class;

        Intent intent = new Intent(activity, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.nav_enter, R.anim.nav_exit);
    }
}
