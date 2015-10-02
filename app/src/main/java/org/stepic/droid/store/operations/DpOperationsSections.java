package org.stepic.droid.store.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.stepic.droid.model.Section;
import org.stepic.droid.store.structure.DbStructureSections;

import java.util.ArrayList;
import java.util.List;

public class DpOperationsSections extends DbOperationsBase {
    public DpOperationsSections(Context context) {
        super(context);
    }

    public void addSection(Section section) {
        ContentValues values = new ContentValues();

        values.put(DbStructureSections.Column.SECTION_ID, section.getId());
        values.put(DbStructureSections.Column.TITLE, section.getTitle());
        values.put(DbStructureSections.Column.SLUG, section.getSlug());
        values.put(DbStructureSections.Column.IS_ACTIVE, section.is_active());
        values.put(DbStructureSections.Column.BEGIN_DATE, section.getBegin_date());
        values.put(DbStructureSections.Column.SOFT_DEADLINE, section.getSoft_deadline());
        values.put(DbStructureSections.Column.HARD_DEADLINE, section.getHard_deadline());


        database.insert(DbStructureSections.SECTIONS, null, values);
    }

    public void deleteSection(Section section) {
        long sectionId = section.getId();
        database.delete(DbStructureSections.SECTIONS,
                DbStructureSections.Column.SECTION_ID + " = " + sectionId,
                null);
    }

    public List<Section> getAllSections() {
        List<Section> sections = new ArrayList<>();

        Cursor cursor = getCursor();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Section section = parseSection(cursor);
            sections.add(section);
            cursor.moveToNext();
        }

        cursor.close();
        return sections;
    }

    private Section parseSection(Cursor cursor) {
        Section section = new Section();
        //ignore id of table
        int columnNumber = 1;

        section.setId(cursor.getInt(columnNumber++));
        section.setTitle(cursor.getString(columnNumber++));
        section.setSlug(cursor.getString(columnNumber++));
        section.setIs_active(cursor.getInt(columnNumber++) > 0);
        section.setBegin_date(cursor.getString(columnNumber++));
        section.setSoft_deadline(cursor.getString(columnNumber++));
        section.setHard_deadline(cursor.getString(columnNumber++));

        return section;
    }

    public void clearCache() {
        List<Section> sections = getAllSections();
        for (Section sectionItem : sections) {
            deleteSection(sectionItem);
        }
    }

    @Override
    public Cursor getCursor() {
        return database.query(DbStructureSections.SECTIONS, DbStructureSections.getUsedColumns(),
                null, null, null, null, null);
    }
}
