package com.objecttracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class ColorTrackerConfigurationTest {

    @Test
    void parseRanges_readsMultipleHsvRanges() {
        List<double[]> ranges = ColorTracker.parseRanges(
                "0,120,70,10,255,255;170,120,70,180,255,255");

        assertEquals(2, ranges.size());
        assertEquals(0.0, ranges.get(0)[0]);
        assertEquals(120.0, ranges.get(0)[1]);
        assertEquals(70.0, ranges.get(0)[2]);
        assertEquals(10.0, ranges.get(0)[3]);
        assertEquals(255.0, ranges.get(0)[4]);
        assertEquals(255.0, ranges.get(0)[5]);

        assertEquals(170.0, ranges.get(1)[0]);
        assertEquals(120.0, ranges.get(1)[1]);
        assertEquals(70.0, ranges.get(1)[2]);
        assertEquals(180.0, ranges.get(1)[3]);
        assertEquals(255.0, ranges.get(1)[4]);
        assertEquals(255.0, ranges.get(1)[5]);
    }

    @Test
    void parseRanges_rejectsInvalidInput() {
        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ColorTracker.parseRanges("0,120,70,10,255"));

        assertEquals("Invalid HSV range format. Expected six comma-separated values in the form lh,ls,lv,uh,us,uv.",
                ex.getMessage());
    }
}
