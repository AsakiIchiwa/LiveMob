package com.codelab.app.data;

import android.content.Context;

import com.codelab.app.R;

import java.util.ArrayList;
import java.util.List;

/** Pure function: profile + progress + installed packs → list of achievements. */
public final class AchievementEngine {

    private AchievementEngine() {}

    public static List<Achievement> computeAll(Context ctx) {
        UserProfile p = ProfileStore.get(ctx).get();
        ProgressStore progress = ProgressStore.get(ctx);
        PackRepository packs = PackRepository.get(ctx);

        // Total lessons completed across all installed packs
        int totalCompleted = 0;
        for (CatalogEntry c : packs.installedEntries()) {
            totalCompleted += progress.countCompleted(c.id, c.lessonCount);
        }
        boolean hasRunCode = totalCompleted > 0 || p.tasksCompleted > 0 || p.xp > 0;

        List<Achievement> out = new ArrayList<>();
        out.add(new Achievement("first_run",
                ctx.getString(R.string.achievement_first_run),
                ctx.getString(R.string.achievement_first_run_desc),
                R.drawable.ic_play_small, hasRunCode));
        out.add(new Achievement("first_lesson",
                ctx.getString(R.string.achievement_first_lesson),
                ctx.getString(R.string.achievement_first_lesson_desc),
                R.drawable.ic_check, totalCompleted >= 1));
        out.add(new Achievement("five_lessons",
                ctx.getString(R.string.achievement_five_lessons),
                ctx.getString(R.string.achievement_five_lessons_desc),
                R.drawable.ic_book, totalCompleted >= 5));
        out.add(new Achievement("streak_3",
                ctx.getString(R.string.achievement_streak_3),
                ctx.getString(R.string.achievement_streak_3_desc),
                R.drawable.ic_fire, p.streakDays >= 3));
        out.add(new Achievement("streak_7",
                ctx.getString(R.string.achievement_streak_7),
                ctx.getString(R.string.achievement_streak_7_desc),
                R.drawable.ic_fire, p.streakDays >= 7));
        out.add(new Achievement("polyglot",
                ctx.getString(R.string.achievement_polyglot),
                ctx.getString(R.string.achievement_polyglot_desc),
                R.drawable.ic_star, packs.installedIds().size() >= 2));
        return out;
    }

    public static int unlockedCount(List<Achievement> all) {
        int n = 0;
        for (Achievement a : all) if (a.unlocked) n++;
        return n;
    }
}
