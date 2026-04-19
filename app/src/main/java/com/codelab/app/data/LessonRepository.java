package com.codelab.app.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience facade over PackRepository + ProgressStore that loads lessons from
 * the active pack and decorates them with their current Status (COMPLETED /
 * IN_PROGRESS / NOT_STARTED).
 */
public final class LessonRepository {

    private LessonRepository() {}

    /** All lessons in the currently active pack, with status filled in. */
    public static List<Lesson> getActive(Context ctx) {
        String activeId = PackRepository.get(ctx).activePackId();
        return getForPack(ctx, activeId);
    }

    public static List<Lesson> getForPack(Context ctx, String packId) {
        if (packId == null) return new ArrayList<>();
        LessonPack pack = PackRepository.get(ctx).loadPack(packId);
        if (pack == null || pack.lessons == null) return new ArrayList<>();
        ProgressStore progress = ProgressStore.get(ctx);
        for (Lesson l : pack.lessons) l.status = progress.statusFor(packId, l.index);
        return pack.lessons;
    }

    public static Lesson findByIndex(Context ctx, String packId, int index) {
        for (Lesson l : getForPack(ctx, packId)) if (l.index == index) return l;
        return null;
    }

    public static LessonPack getActivePack(Context ctx) {
        String id = PackRepository.get(ctx).activePackId();
        return id == null ? null : PackRepository.get(ctx).loadPack(id);
    }
}
