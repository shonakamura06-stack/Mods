package dev.brasschronicles.brass_cartography;

/** Client-side label/position snapshot; it never forces a target chunk to load. */
public record WaypointMarker(String name, String dimension, int x, int z) {}
